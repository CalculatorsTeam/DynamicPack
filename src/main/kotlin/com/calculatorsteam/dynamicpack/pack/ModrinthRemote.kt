package com.calculatorsteam.dynamicpack.pack

import com.calculatorsteam.dynamicpack.DynamicPackMod
import com.calculatorsteam.dynamicpack.Constants
import com.calculatorsteam.dynamicpack.sync.SyncBuilder
import com.calculatorsteam.dynamicpack.sync.SyncProgress
import com.calculatorsteam.dynamicpack.util.*
import com.calculatorsteam.dynamicpack.util.JsonUtil.getBoolean
import com.calculatorsteam.dynamicpack.util.JsonUtil.getInt
import com.calculatorsteam.dynamicpack.util.JsonUtil.getJsonArray
import com.calculatorsteam.dynamicpack.util.JsonUtil.getString
import com.calculatorsteam.dynamicpack.util.JsonUtil.optString
import com.calculatorsteam.dynamicpack.util.LockUtils
import com.calculatorsteam.dynamicpack.util.exception.TranslatableException
import com.google.gson.JsonObject
import java.io.File
import java.io.IOException
import java.util.NoSuchElementException

/**
 * Remote for remote.type = "modrinth"
 */
class ModrinthRemote : Remote() {
    private lateinit var parent: DynamicResourcePack
    private lateinit var cachedCurrentJson: JsonObject
    private lateinit var projectId: String
    private lateinit var gameVersion: String
    private var usesCurrentGameVersion: Boolean = false
    private var noSpecifyGameVersion: Boolean = false

    override fun init(pack: DynamicResourcePack, remote: JsonObject) {
        parent = pack
        cachedCurrentJson = pack.currentJson

        projectId = if (remote.has("project_id"))
            remote.getString("project_id")
        else
            remote.getString("modrinth_project_id")

        val ver = remote.optString("game_version", "no_specify") ?: "no_specify"
        usesCurrentGameVersion = ver.equals("current", ignoreCase = true)
        noSpecifyGameVersion = ver.equals("no_specify", ignoreCase = true)
        gameVersion = if (usesCurrentGameVersion)
            DynamicPackMod.instance.getCurrentGameVersion()
        else ver
    }

    override fun syncBuilder(): SyncBuilder = object : SyncBuilder {
        private var urlsController: UrlsController? = null
        private lateinit var latest: LatestModrinthVersion
        private var latestJson: JsonObject? = null
        private var isUpdateAvailableCache: Boolean? = null
        private var downloaded: Long = 0
        private var updateSizeBytes: Long = 0

        override val downloadedSize: Long
            get() = downloaded

        override val isUpdateAvailable: Boolean
            get() {
                isUpdateAvailableCache?.let { return it }
                val b = _isUpdateAvailable(latestJson)
                isUpdateAvailableCache = b
                return b
            }

        override val updateSize: Long
            get() = updateSizeBytes

        @Throws(Exception::class)
        override fun init(ignoreCaches: Boolean) {
            latestJson = parseModrinthLatestVersionJson()
            latest = LatestModrinthVersion.ofJson(latestJson!!)
            if (isUpdateAvailable) {
                updateSizeBytes = latest.size.toLong()
            }
        }

        @Throws(Exception::class)
        override fun doUpdate(progress: SyncProgress): Boolean {
            if (!isUpdateAvailable) {
                warn("Call doUpdate in modrinth-remote when update not available")
                return false
            }

            progress.setPhase("Downloading resourcepack from modrinth")
            val fileName = latest.url.substringAfterLast('/')
            var tempFile: File? = null
            var attempts = Constants.MAX_ATTEMPTS_TO_DOWNLOAD_FILE

            while (attempts > 0) {
                tempFile = Urls.downloadFileToTemp(
                    latest.url, "dynamicpack_modrinth", ".zip",
                    Constants.MODRINTH_HTTPS_FILE_SIZE_LIMIT,
                    object : UrlsController() {
                        override fun onUpdate(it: UrlsController) {
                            progress.downloading(fileName, it.getPercentage())
                            downloaded = it.getLatest()
                        }
                    }.also { urlsController = it }
                )
                if (urlsController?.isInterrupted() == true) return false
                if (Hashes.sha1sum(tempFile) == latest.fileHash) break

                progress.setPhase("Failed. Downloading again...")
                attempts--
            }
            if (attempts == 0) {
                progress.setPhase("Fatal error.")
                throw RuntimeException("Failed to download correct file from modrinth.")
            }

            progress.setPhase("Updating metadata...")
            cachedCurrentJson.addProperty("version", latest.id)
            cachedCurrentJson.remove("version_number")
            parent.updateJsonLatestUpdate()

            // save client json to temp file
            PackUtil.openPackFileSystem(tempFile) { parent.saveClientFile(it) }

            if (parent.isZip()) {
                progress.setPhase("Unlocking file.")
                LockUtils.closeFile(parent.getLocation())

                progress.setPhase("Move files...")
                PathUtil.moveFile(tempFile, parent.getLocation())
            } else {
                progress.setPhase("Extracting files...")
                PathUtil.recursiveDeleteDirectory(parent.getLocation())
                PathUtil.unzip(tempFile, parent.getLocation())
                PathUtil.delete(tempFile?.toPath())
            }

            // save client json again
            progress.setPhase("Saving dynamicmcpack.json")
            parent.saveClientFile()
            progress.setPhase("Success")
            return true
        }

        override fun interrupt() {
            urlsController?.interrupt()
        }
    }

    fun getCurrentUnique(): String =
        cachedCurrentJson.optString("version", "") ?: ""

    fun getCurrentVersionNumber(): String =
        cachedCurrentJson.optString("version_number", "") ?: ""

    fun getApiVersionsUrl(): String =
        "https://api.modrinth.com/v2/project/$projectId/version"

    fun getProjectId(): String = projectId
    fun isUsesCurrentGameVersion(): Boolean = usesCurrentGameVersion

    @Throws(IOException::class)
    fun parseModrinthLatestVersionJson(): JsonObject {
        val content = Urls.parseTextContent(getApiVersionsUrl(), Constants.MOD_MODTINTH_API_LIMIT)
            ?: throw IOException("Failed to fetch Modrinth API response")
        val versions = JsonUtil.arrayFromString(content)
        for (o in versions) {
            val version = o.asJsonObject
            if (noSpecifyGameVersion) return version

            val gameVersions = version.getAsJsonArray("game_versions")
            val supportGameVersion = gameVersions
                .map { it.asString }
                .any { it == gameVersion }
            if (supportGameVersion) return version
        }
        throw TranslatableException(
            "Could not find the latest version on modrinth with suitable parameters",
            "dynamicpack.exceptions.pack.remote.modrinth.not_found_latest_version"
        )
    }

    @Throws(IOException::class)
    override fun checkUpdateAvailable(): Boolean {
        val latest = parseModrinthLatestVersionJson()
        return _isUpdateAvailable(latest)
    }

    private fun _isUpdateAvailable(latest: JsonObject?): Boolean {
        if (latest == null) {
            warn("Latest version not available for this game_version")
            return false
        }
        if (latest.optString("version_number", "") == getCurrentVersionNumber()) {
            debug("Version number equal. Update not available")
            return false
        }
        val id = latest.getString("id")
        debug("Version remote.id=$id; current=${getCurrentUnique()}")
        return getCurrentUnique() != id
    }

    /**
     * Data-object for version in Modrinth API. Uses only for latest version.
     */
    data class LatestModrinthVersion(
        val id: String,
        val versionNumber: String,
        val url: String,
        val fileHash: String,
        val size: Int
    ) {
        companion object {
            fun ofJson(latest: JsonObject): LatestModrinthVersion {
                val latestId = latest.getString("id")
                val latestVersionNumber = latest.getString("version_number")
                val files = latest.getAsJsonArray("files")
                for (element in files) {
                    val file = element.asJsonObject
                    if (file.getAsJsonPrimitive("primary").asBoolean) {
                        val url = file.getAsJsonPrimitive("url").asString
                        val size = file.getAsJsonPrimitive("size").asInt
                        val hash = file.getAsJsonObject("hashes").getAsJsonPrimitive("sha1").asString
                        return LatestModrinthVersion(latestId, latestVersionNumber, url, hash, size)
                    }
                }
                throw NoSuchElementException("File json-object with primary=true not found... Modrinth API???")
            }
        }
    }

    fun debug(s: String) = parent.debug(s)
    fun warn(s: String) = parent.warn(s)
}