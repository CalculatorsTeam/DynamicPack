package com.calculatorsteam.dynamicpack.util

import com.calculatorsteam.dynamicpack.DynamicPackMod
import com.calculatorsteam.dynamicpack.InputValidator
import com.calculatorsteam.dynamicpack.Constants
import com.calculatorsteam.dynamicpack.util.log.NetworkStat
import com.calculatorsteam.dynamicpack.util.log.Out
import java.io.*
import java.net.URI
import java.net.URL
import java.net.URLConnection
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.util.zip.GZIPInputStream

/**
 * Safe network utils
 */
object Urls {
    @JvmStatic
    fun isFileDebugSchemeAllowed(): Boolean = Constants.isFileDebugSchemeAllowed()

    @JvmStatic
    fun isHTTPTrafficAllowed(): Boolean = Constants.isHTTPTrafficAllowed()

    /**
     * Parse text content from URL (no progress).
     */
    @JvmStatic
    @Throws(IOException::class)
    fun parseTextContent(url: String, limit: Long): String? =
        parseTextContent(url, limit, null)

    /**
     * Parse text content from url
     */
    @JvmStatic
    @Throws(IOException::class)
    fun parseTextContent(url: String, limit: Long, progress: UrlsController?): String? =
        _parseTextFromStream(_getInputStreamOfUrl(url, limit, progress), progress)

    /**
     * Parse GZip compressed content from url
     */
    @JvmStatic
    @Throws(IOException::class)
    fun parseTextGZippedContent(url: String, limit: Long, progress: UrlsController?): String? =
        _parseTextFromStream(GZIPInputStream(_getInputStreamOfUrl(url, limit, progress)), progress)

    /**
     * Create temp zipFile and download to it from url.
     */
    @JvmStatic
    @Throws(IOException::class)
    fun downloadFileToTemp(
        url: String,
        tmpPrefix: String,
        tmpSuffix: String,
        limit: Long,
        controller: UrlsController?
    ): File {
        val file = File.createTempFile(tmpPrefix, tmpSuffix)
        _getInputStreamOfUrl(url, limit, controller).use { input ->
            Files.newOutputStream(file.toPath()).use { output ->
                _transferStreams(input, output, controller)
            }
        }
        return file
    }

    // --- Internal low-level helpers ---

    @Throws(IOException::class)
    internal fun _getInputStreamOfUrl(url: String, sizeLimit: Long, controller: UrlsController?): InputStream {
        if (" " in url) throw IOException("URL can't contain spaces!")
        InputValidator.throwIsUrlInvalid(url)

        return when {
            url.startsWith("file_debug_only://") -> {
                if (!isFileDebugSchemeAllowed()) {
                    throw RuntimeException("Not allowed scheme.")
                }
                val gameDir = DynamicPackMod.gameDir
                val file = File(gameDir, url.removePrefix("file_debug_only://"))
                FileInputStream(file)
            }
            url.startsWith("http://") -> {
                if (!isHTTPTrafficAllowed()) {
                    throw RuntimeException("HTTP (not secure) not allowed scheme.")
                }
                throwIsUrlNotTrust(url)
                __unsafeInputStreamFromUrl(url, sizeLimit, controller)
            }
            url.startsWith("https://") -> {
                throwIsUrlNotTrust(url)
                __unsafeInputStreamFromUrl(url, sizeLimit, controller)
            }
            else -> throw RuntimeException("Unsupported scheme for url $url")
        }
    }

    /**
     * # Unsafe: returns stream with only sizeLimit check.
     */
    @Throws(IOException::class)
    private fun __unsafeInputStreamFromUrl(
        url: String,
        sizeLimit: Long,
        controller: UrlsController?
    ): InputStream {
        val metaSize = Constants.HTTP_MINIMAL_HEADER_SIZE + url.length
        return NetworkStat.runNetworkTask(metaSize.toLong()) {
            val connection: URLConnection = URI(url).toURL().openConnection()
            val length = connection.contentLengthLong
            if (length > sizeLimit) {
                throw RuntimeException("File size exceeds $length bytes > limit $sizeLimit; url=$url")
            }
            controller?.updateMax(length)
            connection.getInputStream()
        }
    }

    @Throws(IOException::class)
    internal fun _parseTextFromStream(stream: InputStream, controller: UrlsController?): String? =
        stream.use { input ->
            val out = ByteArrayOutputStream()
            val buf = ByteArray(Constants.URLS_BUFFER_SIZE)
            val isNetwork = isNetwork(input)

            readLoop(input, buf, controller, isNetwork) { data, off, len ->
                out.write(data, off, len)
            }

            out.toString(StandardCharsets.UTF_8)
        }

    /**
     * Transfer streams and close all
     */
    @Throws(IOException::class)
    private fun _transferStreams(inputStream: InputStream, outputStream: OutputStream, controller: UrlsController?) {
        inputStream.use { inp ->
            outputStream.use { outp ->
                BufferedInputStream(inp).use { inBuf ->
                    val buf = ByteArray(Constants.URLS_BUFFER_SIZE)
                    val isNetwork = isNetwork(inp)

                    readLoop(inBuf, buf, controller, isNetwork) { data, off, len ->
                        outp.write(data, off, len)
                    }
                }
            }
        }
    }

    @Throws(IOException::class)
    internal fun _transferStreamsWithHash(
        hash: String,
        inputStream: InputStream,
        outputStream: OutputStream,
        controller: UrlsController?
    ) {
        val buf = ByteArray(Constants.URLS_BUFFER_SIZE)
        val isNetwork = isNetwork(inputStream)

        val bytes = BufferedInputStream(inputStream).use { inBuf ->
            val out = ByteArrayOutputStream()
            readLoop(inBuf, buf, controller, isNetwork) { data, off, len ->
                out.write(data, off, len)
            }
            out.toByteArray()
        }

        val calcHash = Hashes.sha1sum(bytes)
        if (calcHash == hash) {
            _transferStreams(ByteArrayInputStream(bytes), outputStream, null)
        } else {
            throw SecurityException("Hash mismatch: expected=$hash actual=$calcHash")
        }
    }

    // --- Core loop to eliminate duplicates ---

    private fun readLoop(
        input: InputStream,
        buf: ByteArray,
        controller: UrlsController?,
        isNetwork: Boolean,
        onBytes: (data: ByteArray, offset: Int, length: Int) -> Unit
    ): Long {
        var total = 0L
        var bytesRead: Int
        while (true) {
            if (UrlsController.isInterrupted(controller)) {
                Out.debug("interrupted readLoop")
                break
            }
            val start = System.currentTimeMillis()
            bytesRead = input.read(buf)
            if (bytesRead == -1) break
            onBytes(buf, 0, bytesRead)
            total += bytesRead
            UrlsController.updateCurrent(controller, total)
            if (isNetwork) {
                Constants.debugNetwork(bytesRead, total)
                NetworkStat.addLap(System.currentTimeMillis() - start, bytesRead.toLong())
            }
        }
        return total
    }

    private fun isNetwork(isrc: InputStream): Boolean = isrc !is ByteArrayInputStream

    @Throws(IOException::class)
    private fun throwIsUrlNotTrust(url: String) {
        if (!Constants.isUrlHostTrusted(url) && Constants.isBlockAllNotTrustedNetworks()) {
            throw SecurityException("Url '$url' host is not trusted!")
        }
    }
}