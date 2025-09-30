package com.calculatorsteam.dynamicpack.pack.dynamicrepo

import com.calculatorsteam.dynamicpack.InputValidator
import com.calculatorsteam.dynamicpack.Constants
import com.calculatorsteam.dynamicpack.pack.DynamicResourcePack
import com.calculatorsteam.dynamicpack.sync.SyncBuilder
import com.calculatorsteam.dynamicpack.sync.SyncProgress
import com.calculatorsteam.dynamicpack.util.*
import com.calculatorsteam.dynamicpack.util.JsonUtil.getLong
import com.calculatorsteam.dynamicpack.util.JsonUtil.getString
import com.calculatorsteam.dynamicpack.util.JsonUtil.optBoolean
import com.calculatorsteam.dynamicpack.util.JsonUtil.optInt
import com.calculatorsteam.dynamicpack.util.JsonUtil.optLong
import com.calculatorsteam.dynamicpack.util.JsonUtil.optString
import com.calculatorsteam.dynamicpack.util.LockUtils
import com.calculatorsteam.dynamicpack.util.log.NetworkStat
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory

class DynamicRepoSyncBuilder(
    private val pack: DynamicResourcePack,
    private val remote: DynamicRepoRemote
) : SyncBuilder {

    private val oldestFilesList = mutableSetOf<String>()
    private var doNotDeleteOldestFiles = false
    private val dynamicFiles = mutableMapOf<String, DynamicFile>()

    private var updateAvailable: Boolean = false
    private var _updateSize: Long = 0
    private var _downloadedSize: Long = 0
    private lateinit var repoJson: JsonObject

    private var reloadRequired: Boolean = false
    private var interrupted: Boolean = false

    override fun init(ignoreCaches: Boolean) {
        updateAvailable = ignoreCaches || remote.checkUpdateAvailable()
        if (updateAvailable) {
            val packUrlContent = Urls.parseTextContent(
                remote.packUrl,
                Constants.MOD_FILES_LIMIT,
                object : UrlsController() {
                    override fun isInterrupted() = interrupted
                }
            )?.trim() ?: throw RuntimeException("Empty repo response")

            repoJson = JsonUtil.fromString(packUrlContent)

            val formatVersion = repoJson.get("formatVersion").asLong
            if (formatVersion != 1L) {
                throw RuntimeException("Incompatible formatVersion: $formatVersion")
            }

            val minBuild = repoJson.optLong("minimal_mod_build", Constants.VERSION_BUILD)
            if (minBuild > Constants.VERSION_BUILD) {
                throw RuntimeException(
                    "Incompatible DynamicPack Mod version: required $minBuild, current=${Constants.VERSION_BUILD}"
                )
            }

            val remoteName = repoJson.getString("name")
            if (!InputValidator.isDynamicPackNameValid(remoteName)) {
                throw RuntimeException("Remote name not valid")
            }

            PackUtil.openPackFileSystem(pack.getLocation()) { fs ->
                PathUtil.walkScan(oldestFilesList, fs)
            }

            try {
                checkContents(repoJson.getAsJsonArray("contents"))
                remote.notifyNewRemoteJson(repoJson)

                calcActiveContents().forEach { processContentInit(it) }
            } catch (e: Exception) {
                doNotDeleteOldestFiles = true
                throw e
            }
        }
    }

    private fun checkContents(contents: JsonArray) {
        val ids = mutableSetOf<String>()
        for (elem in contents) {
            val obj = elem.asJsonObject
            val id = obj.getString("id")
            InputValidator.throwIsContentIdInvalid(id)
            if (!ids.add(id)) {
                throw RuntimeException("Duplicate content found: $id")
            }
        }
    }

    override val downloadedSize: Long
        get() = _downloadedSize

    override fun doUpdate(progress: SyncProgress): Boolean {
        progress.setPhase("Opening a pack file-system")
        PackUtil.openPackFileSystem(pack.getLocation(), LockUtils.createFileFinalizer(pack.getLocation())) { packFs ->
            internalProcessDynamicFiles(progress, packFs)

            if (!doNotDeleteOldestFiles && !interrupted) {
                progress.setPhase("Deleting unnecessary files")
                oldestFilesList.forEach { s ->
                    val pathToFile = packFs.resolve(s)
                    if (pathToFile.fileName.toString().equals(Constants.CLIENT_FILE, true)) return@forEach

                    progress.deleted(pathToFile)
                    PathUtil.nioSmartDelete(pathToFile)
                    markReloadRequired(s)
                }
            }

            progress.setPhase("Updating metadata...")
            pack.packJson.getAsJsonObject("current")
                .addProperty("build", repoJson.getLong("build"))
            pack.updateJsonLatestUpdate()
            pack.saveClientFile(packFs)
        }
        progress.setPhase("Success")
        return reloadRequired
    }

    override fun interrupt() {
        interrupted = true
    }

    override val isUpdateAvailable: Boolean
        get() = updateAvailable

    override val updateSize: Long
        get() = _updateSize

    private fun processContentInit(jsonContent: JsonObject) {
        val id = jsonContent.getString("id")
        InputValidator.throwIsContentIdInvalid(id)
        var url = jsonContent.getString("url")
        var urlCompressed = jsonContent.optString("url_compressed", null)
        val compressSupported = urlCompressed != null
        checkPathSafety(url)
        val hash = jsonContent.getString("hash")

        url = "${remote.url}/$url"
        if (compressSupported) {
            checkPathSafety(urlCompressed)
            urlCompressed = "${remote.url}/$urlCompressed"
        }

        val content = if (compressSupported) {
            Urls.parseTextGZippedContent(urlCompressed!!, Constants.GZIP_LIMIT, null)
        } else {
            Urls.parseTextContent(url, Constants.MOD_FILES_LIMIT, null)
        } ?: throw RuntimeException("Can't load content $url")

        val receivedHash = Hashes.sha1sum(content.toByteArray(StandardCharsets.UTF_8))
        if (hash != receivedHash) {
            throw SecurityException("Hash mismatch for content $url")
        }

        val jsonContentD2 = JsonUtil.fromString(content)
        PackUtil.openPackFileSystem(remote.parent.getLocation(), LockUtils.createFileFinalizer(pack.getLocation())) { packFs ->
            val formatVersion = jsonContentD2.getLong("formatVersion")
            if (formatVersion != 1L) throw RuntimeException("Incompatible formatVersion=$formatVersion")

            val c = jsonContentD2.getAsJsonObject("content")
            val parentPath = c.optString("parent", "") ?: ""
            val rem = c.optString("remote_parent", "") ?: ""
            val files = c.getAsJsonObject("files")

            var processedFiles = 0
            for (fileEntry in files.keySet()) {
                if (interrupted) return@openPackFileSystem
                var pathValidated = false
                try {
                    val relPath = getAndCheckPath(parentPath, fileEntry)
                    InputValidator.throwIsPathInvalid(relPath)
                    pathValidated = true
                    val filePath = packFs.resolve(relPath)

                    if (filePath.fileName.toString().contains(Constants.CLIENT_FILE)) {
                        warn("File ${Constants.CLIENT_FILE} can't be updated remotely!")
                        continue
                    }

                    val fileRemoteUrl = getUrlFromPathAndCheck(rem, relPath)
                    val fileExtra = files.getAsJsonObject(fileEntry)
                    val fileHash = fileExtra.getString("hash")
                    val fileSize = fileExtra.optInt("size", Int.MAX_VALUE)

                    if (!InputValidator.isHashValid(fileHash)) {
                        warn("Hash not valid for file: $relPath")
                        continue
                    }

                    oldestFilesList.remove(relPath)

                    var isNeedOverwrite = !Files.exists(filePath) ||
                            Hashes.sha1sum(filePath) != fileHash

                    if (dynamicFiles.containsKey(relPath)) {
                        warn("File duplicates across contents: $relPath")
                        _updateSize -= dynamicFiles[relPath]!!.size.toLong()
                        isNeedOverwrite = true
                    }

                    if (isNeedOverwrite) {
                        val dyn = DynamicFile(fileRemoteUrl, relPath, fileSize, fileHash)
                        _updateSize += fileSize.toLong()
                        dynamicFiles[relPath] = dyn
                    }

                    processedFiles++
                } catch (e: Exception) {
                    val errorFile = if (pathValidated) fileEntry else "(failed to validate)"
                    error("Error while process file $errorFile", e)
                }
            }
            println("Total initialized files in content '$id': $processedFiles")
        }
    }

    private fun internalProcessDynamicFiles(progress: SyncProgress, packFs: Path) {
        debug("internalProcessDynamicFiles begin")
        NetworkStat.speedMultiplier = DOWNLOAD_THREADS_COUNT.toLong()
        val tempPath: Path? = if (pack.isZip()) {
            val tmp = File(System.getProperty("java.io.tmpdir") + File.separator + Constants.TEMP_DIR_NAME, pack.name).toPath()
            if (!Files.exists(tmp)) {
                PathUtil.createDirsToFile(tmp)
                Files.createDirectory(tmp)
            }
            tmp
        } else null

        val executor = getExecutor()
        CompletableFuture.supplyAsync({ dynamicFiles.values }, executor)
            .thenCompose { files ->
                val futs = files.map { file ->
                    CompletableFuture.supplyAsync({
                        if (interrupted) throw InterruptedException("Interrupted")
                        downloadFile(tempPath ?: packFs, file, progress)
                        file
                    }, executor).exceptionally {
                        error("Error while download a file", it)
                        null
                    }
                }
                CompletableFuture.allOf(*futs.toTypedArray())
                    .thenApply { futs.mapNotNull { it.join() } }
            }.whenComplete { files, th ->
                if (interrupted) return@whenComplete
                if (th == null) {
                    files.forEach { file ->
                        if (file.downloadedPath == null) {
                            warn("Downloaded file with null path: $file")
                            return@forEach
                        }
                        markReloadRequired(file)
                        if (tempPath != null) {
                            try {
                                val dest = packFs.resolve(file.path)
                                val src = file.downloadedPath!!
                                PathUtil.createDirsToFile(dest)
                                Files.move(src, dest, StandardCopyOption.REPLACE_EXISTING)
                                PathUtil.nioSmartDelete(src)
                            } catch (e: Exception) {
                                error("Error while moving file ${file.path}", e)
                            }
                        }
                    }
                } else throw RuntimeException(th)
            }.join()
        NetworkStat.speedMultiplier = 1
        debug("internalProcessDynamicFiles end")
    }

    private fun downloadFile(rootPath: Path, dynamicFile: DynamicFile, progress: SyncProgress) {
        val filePath = rootPath.resolve(dynamicFile.path)
        if (filePath.fileName.toString().contains(Constants.CLIENT_FILE)) {
            warn("File ${Constants.CLIENT_FILE} can't be updated remotely!")
            return
        }
        if (PathUtil.isPathFileExists(filePath)) {
            if (Hashes.sha1sum(filePath).equals(dynamicFile.hash, true)) {
                warn("File ${dynamicFile.path} skipped: already exists with same hash")
                dynamicFile.downloadedPath = filePath
                _downloadedSize += Files.size(filePath)
                return
            }
        }
        PackUtil.downloadPackFile(dynamicFile.url, filePath, dynamicFile.hash, object : UrlsController() {
            private var fileSize: Long = 0
            override fun onUpdate(it: UrlsController) {
                progress.downloading(filePath.fileName.toString(), it.getPercentage())
                _downloadedSize -= fileSize
                fileSize = it.getLatest()
                _downloadedSize += fileSize
            }
            override fun isInterrupted() = interrupted
        })
        if (interrupted) {
            progress.setPhase("Interrupted")
            return
        }
        dynamicFile.downloadedPath = filePath
        progress.setPhase("File ${dynamicFile.path} downloaded!")
    }

    private fun getExecutor(): ExecutorService {
        val execNum = executorCounter++
        return Executors.newFixedThreadPool(DOWNLOAD_THREADS_COUNT, object : ThreadFactory {
            private var count = 1
            override fun newThread(r: Runnable) = Thread(r, "DownloadWorker$execNum-${count++}")
        })
    }

    private fun getUrlFromPathAndCheck(remoteParent: String, path: String): String {
        checkPathSafety(remoteParent)
        return if (remoteParent.isEmpty()) "${remote.url}/$path"
        else "${remote.url}/$remoteParent/$path"
    }

    companion object {
        var DOWNLOAD_THREADS_COUNT = 8
        private var executorCounter = 0

        @JvmStatic
        fun getAndCheckPath(parent: String, path: String): String {
            checkPathSafety(path)
            checkPathSafety(parent)
            return if (parent.isEmpty()) path else "$parent/$path"
        }

        @JvmStatic
        fun checkPathSafety(s: String) {
            if (arrayOf("://", "..", "  ", ".exe", ":", ".jar").any { s.contains(it) }) {
                throw SecurityException("This path not safe: $s")
            }
        }
    }

    private fun calcActiveContents(): List<JsonObject> {
        val active = mutableListOf<JsonObject>()
        val arr = repoJson.getAsJsonArray("contents")
        for (content in arr) {
            val obj = content.asJsonObject
            val id = obj.getString("id")
            InputValidator.throwIsContentIdInvalid(id)
            val defaultActive = obj.optBoolean("default_active", true)
            val required = obj.optBoolean("required", false)
            if (required || remote.preferences.isContentActive(id, defaultActive)) {
                active.add(obj)
            }
        }
        return active
    }

    fun isReloadRequired() = reloadRequired

    private fun markReloadRequired(obj: Any) {
        if (!reloadRequired) debug("Now reload is required because $obj")
        reloadRequired = true
    }

    private fun debug(s: String) = pack.debug(s)
    private fun error(s: String, e: Throwable) = pack.error(s, e)
    private fun warn(s: String) = pack.warn(s)
    private fun println(s: String) = pack.println(s)
}