package com.calculatorsteam.dynamicpack.util

import com.calculatorsteam.dynamicpack.Constants
import com.calculatorsteam.dynamicpack.util.exception.FailedOpenPackFileSystemException
import com.calculatorsteam.dynamicpack.util.log.FileLog
import com.calculatorsteam.dynamicpack.util.log.Out
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.net.URI
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path

/**
 * Pack utilities: open ZIP or folder as FileSystem, download packs.
 */
object PackUtil {

    /**
     * Open file (or dir) as nio.Path.
     *
     * If exception in consumer → filesystem closed normally.
     */
    @JvmStatic
    @Throws(Exception::class)
    fun openPackFileSystem(
        file: File?,                      // теперь nullable
        preClose: Runnable? = null,
        consumer: (Path) -> Unit
    ) {
        val realFile = file ?: throw FileNotFoundException("File is null")

        if (!realFile.exists()) {
            throw FileNotFoundException(realFile.canonicalPath)
        }

        when {
            realFile.isDirectory -> {
                consumer(realFile.toPath())
            }

            realFile.isFile && realFile.name.lowercase().endsWith(".zip") -> {
                val env = mapOf("create" to "true")
                val uri = URI.create("jar:" + realFile.toPath().toUri())

                var caught: Exception? = null
                FileSystems.newFileSystem(uri, env).use { fs ->
                    try {
                        consumer(fs.getPath(""))
                    } catch (e: Exception) {
                        caught = e
                    } finally {
                        preClose?.run()
                    }
                }
                if (caught != null) throw caught
            }

            else -> throw FailedOpenPackFileSystemException(
                "Failed to recognize pack filesystem: not dir or zip"
            )
        }
    }

    /**
     * Download file for dynamic_repo.
     * Retries on failure up to [Constants.MAX_ATTEMPTS_TO_DOWNLOAD_FILE].
     *
     * @throws IOException if latest attempt failed, rethrown.
     */
    @JvmStatic
    @Throws(IOException::class)
    fun downloadPackFile(url: String, path: Path?, hash: String, controller: UrlsController) {
        val target = path ?: throw IOException("Path is null")
        val maxAttempts = Constants.MAX_ATTEMPTS_TO_DOWNLOAD_FILE

        repeat(maxAttempts) { attempt ->
            try {
                PathUtil.createDirsToFile(target)

                if (Files.exists(target)) {
                    PathUtil.delete(target)
                }
                PathUtil.createFile(target)

                try {
                    Urls._transferStreamsWithHash(
                        hash,
                        Urls._getInputStreamOfUrl(
                            url,
                            Constants.DYNAMIC_PACK_HTTPS_FILE_SIZE_LIMIT,
                            controller
                        ),
                        Files.newOutputStream(target),
                        controller
                    )
                    FileLog.writtenByUrl(target, url)
                } catch (e: Exception) {
                    throw RuntimeException(
                        "File $target download error. From url: $url. Expected hash: $hash", e
                    )
                }
                return // success → exit method

            } catch (e: Exception) {
                Out.error("downloadPackFile. Attempt=${attempt + 1}/$maxAttempts", e)
                if (attempt == maxAttempts - 1) throw e
            }
        }
    }
}