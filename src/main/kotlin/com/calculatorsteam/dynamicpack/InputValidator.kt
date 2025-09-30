package com.calculatorsteam.dynamicpack

import com.calculatorsteam.dynamicpack.util.log.Out
import java.net.URISyntaxException
import java.nio.charset.StandardCharsets
import java.util.regex.Pattern

/**
 * Validates for [user/resourcepack-creator] input values
 */
object InputValidator {
    private val CONTENT_ID_PATTERN: Pattern = Pattern.compile("^[a-z0-9_:-]{2,128}$")
    private val PATH_PATTERN: Pattern = Pattern.compile("^[A-Za-z0-9_./() +-]{0,255}$")
    private val URL_PATTERN: Pattern = Pattern.compile(
        "(https?://)?(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\+.~#?&//=]*)"
    )

    /**
     * dynamic content id valid?
     */
    @JvmStatic
    fun isDynamicContentIdValid(contentId: String?): Boolean =
        contentId != null && CONTENT_ID_PATTERN.matcher(contentId).matches()

    /**
     * Throw if content id invalid
     */
    @JvmStatic
    fun throwIsContentIdInvalid(contentId: String?) {
        if (!isDynamicContentIdValid(contentId)) {
            throw RuntimeException("Id of content is not valid: ${safeOutput(contentId ?: "<null>")}")
        }
    }

    /**
     * Is dynamic_repo content name valid?
     */
    @JvmStatic
    fun isDynamicContentNameValid(contentName: String?): Boolean =
        contentName?.trim()?.let {
            it.length < 64 && it.isNotEmpty() &&
                    !it.contains("\n") && !it.contains("\r") && !it.contains("\b")
        } ?: false

    /**
     * Dynamic pack name valid?
     */
    @JvmStatic
    fun isDynamicPackNameValid(name: String?): Boolean =
        name?.trim()?.let {
            it.length < 64 && it.isNotEmpty() &&
                    !it.contains("\n") && !it.contains("\r") && !it.contains("\b")
        } ?: false

    /**
     * Throw if local path invalid
     */
    @JvmStatic
    fun throwIsPathInvalid(path: String?) {
        if (path == null) {
            throw SecurityException("Null path", NullPointerException("path to valid is null"))
        }

        val trim = path.trim()
        if (trim.length < 2 || !PATH_PATTERN.matcher(path).matches()) {
            val safe = path.toByteArray(StandardCharsets.US_ASCII).toString(StandardCharsets.US_ASCII)
            throw SecurityException("Not valid path: $safe")
        }
    }

    /**
     * Is URL valid?
     */
    @JvmStatic
    fun isUrlValid(url: String): Boolean {
        try {
            if (Constants.isLocalHostAllowed() && Constants.getUrlHost(url) == "localhost") {
                Out.warn("isUrlValid return true for localhost! Behavior only when isLocalHostAllowed()=true")
                return true
            }
        } catch (_: URISyntaxException) { }
        return URL_PATTERN.matcher(url).matches()
    }

    /**
     * Throw if url invalid
     */
    @JvmStatic
    fun throwIsUrlInvalid(url: String?) {
        if (url == null) {
            throw SecurityException("null", NullPointerException("url to valid is null"))
        }
        if (!isUrlValid(url)) {
            throw SecurityException("Not valid url: ${safeOutput(url)}")
        }
    }

    /**
     * SHA1 valid?
     */
    @JvmStatic
    fun isHashValid(hash: String?): Boolean =
        hash != null && hash.length == 40 && !hash.contains(" ")

    private fun safeOutput(s: String): String {
        val short = if (s.length >= 100) s.take(100) else s
        return short.toByteArray(StandardCharsets.US_ASCII).toString(StandardCharsets.US_ASCII)
    }
}