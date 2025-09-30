package com.calculatorsteam.dynamicpack

import com.calculatorsteam.dynamicpack.util.log.Out
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.IOException
import java.net.URI
import java.net.URISyntaxException

object Constants {
    // DISABLE ALL DEBUG IN OFFICIAL RELEASES
    const val DEBUG: Boolean = false // Don't forget to disable in release
    const val DEBUG_ALLOW_UNSECURE: Boolean = false
    @JvmField var DEBUG_LOGS: Boolean = false

    const val VERSION_BUILD: Long = 50
    const val VERSION_NAME_MOD: String = "1.2"
    /*? if >=1.21.6 {*/
    const val VERSION_NAME_BRANCH: String = "mc1.21.8"
    /*?} else if >=1.21.5 {*/
    /*const val VERSION_NAME_BRANCH: String = "mc1.21.5"
    *//*?} else if >=1.21.4 {*/
    /*const val VERSION_NAME_BRANCH: String = "mc1.21.4"
    *//*?} else if >=1.21.2 {*/
    /*const val VERSION_NAME_BRANCH: String = "mc1.21.3"
    *//*?} else if >=1.21 {*/
    /*const val VERSION_NAME_BRANCH: String = "mc1.21.1"
    *//*?} else if >=1.20 {*/
    /*const val VERSION_NAME_BRANCH: String = "mc1.20.1"
    *//*?}*/
    val VERSION_NAME: String = "$VERSION_NAME_MOD+$VERSION_NAME_BRANCH${if (DEBUG) "-debug" else ""}"
    const val MOD_ID: String = "dynamicpack"

    // NOTE: for increase contact to mod developer.
    val DYNAMIC_PACK_HTTPS_FILE_SIZE_LIMIT: Long = megabyte(8)   // 8 MB
    val MODRINTH_HTTPS_FILE_SIZE_LIMIT: Long = megabyte(1024)    // 1 GB
    val MOD_MODTINTH_API_LIMIT: Long = megabyte(8)               // 8 MB
    val GZIP_LIMIT: Long = megabyte(50)                         // 50 MB
    val MOD_FILES_LIMIT: Long = megabyte(8)
    const val MODRINTH_URL: String = "https://modrinth.com/mod/dynamicpack"
    val NETWORK_STAT_RESET_LIMIT: Long = megabyte(3)

    // Settings
    const val MAX_ATTEMPTS_TO_DOWNLOAD_FILE: Int = 3
    const val TEMP_DIR_NAME: String = "dynamicpack_f02ffd55_cd44_458a_8d58_e31b11313a53"
    @JvmField var URLS_BUFFER_SIZE: Int = 1024

    // const
    const val HTTP_MINIMAL_HEADER_SIZE: Long = 24
    const val CLIENT_FILE: String = "dynamicmcpack.json"
    const val MINECRAFT_META: String = "pack.mcmeta"
    val UNKNOWN_PACK_MCMETA: String = """
        {
          "pack": {
            "pack_format": 17,
            "description": "Unknown DynamicPack resource-pack..."
          }
        }
    """.trimIndent()

    @JvmField val GSON: Gson = GsonBuilder().setPrettyPrinting().create()

    // --- Hosts ---
    private val ALLOWED_HOSTS: MutableSet<String> = buildSet {
        add("modrinth.com")
        add("github.com")
        add("github.io")
        add("githubusercontent.com") // better use github pages
        if (isLocalHostAllowed()) {
            add("localhost")
        }
    }.toMutableSet()

    /**
     * API FOR MODPACKERS etc all-in-one packs
     * @param host host to add.
     * @param requester any object. It is recommended that .toString explicitly give out your name.
     */
    @Throws(Exception::class)
    internal fun addAllowedHosts(host: String?, requester: Any?) {
        if (host == null || requester == null) {
            Out.securityWarning("Try to add allowed hosts is failed: null host or requester")
            throw Exception("Try to add allowed hosts is failed: null host or requester")
        }

        Out.securityWarning("==== SECURITY WARNING ====")
        Out.securityWarning("# The DynamicPack mod limits the hosts it can interact with.")
        Out.securityWarning("# But a certain requester allowed the mod another host to interact with")
        Out.securityWarning("# ")
        Out.securityWarning("# Host: $host")
        Out.securityWarning("# Requester: $requester")
        Out.securityWarning("# StackTrace:")
        Out.securityStackTrace()
        Out.securityWarning("# ")
        Out.securityWarning("===========================")

        ALLOWED_HOSTS += host
    }

    @Throws(URISyntaxException::class)
    fun getUrlHost(url: String): String? = URI(url).host

    @Throws(IOException::class)
    fun isUrlHostTrusted(url: String): Boolean {
        return try {
            val host = getUrlHost(url) ?: return false
            when {
                host in ALLOWED_HOSTS -> true
                ALLOWED_HOSTS.any { host.endsWith(".$it") } -> true
                else -> {
                    Out.warn("Check trusted(false): $host")
                    false
                }
            }
        } catch (e: Exception) {
            throw IOException("Error while check url for trust", e)
        }
    }

    fun megabyte(mb: Long): Long = 1024L * 1024L * mb

    fun speedToString(bytesPerSec: Long): String = when {
        bytesPerSec >= 1024 * 1024 -> "${bytesPerSec / 1024 / 1024} MiB/s"
        bytesPerSec >= 1024        -> "${bytesPerSec / 1024} KiB/s"
        else                       -> "$bytesPerSec B/s"
    }

    fun secondsToString(s: Long): String = when {
        s > 3600 -> "${s / 3600}h"
        s > 60   -> "${s / 60}m"
        else     -> "${s}s"
    }

    fun isBlockAllNotTrustedNetworks(): Boolean = true

    // TRUE FOR ALL PUBLIC VERSION!!!!!!
    // false is equal not safe!1!!!
    fun isRelease(): Boolean = !DEBUG

    // localhost allowed RELEASE=false
    fun isLocalHostAllowed(): Boolean = DEBUG_ALLOW_UNSECURE

    // file_debug_only:// allowed RELEASE=false
    fun isFileDebugSchemeAllowed(): Boolean = DEBUG_ALLOW_UNSECURE

    // http:// allowed RELEASE=false
    fun isHTTPTrafficAllowed(): Boolean = false

    fun debugNetwork(bytesRead: Int, total: Long) {
        if (isRelease()) return
        if (true) return  // Забавная проверка :) оставим семантику как есть.

        try {
            Thread.sleep(200)
        } catch (e: InterruptedException) {
            throw RuntimeException(e)
        }
    }

    fun isDebugLogs(): Boolean = DEBUG_LOGS

    fun isDebugMessageOnWorldJoin(): Boolean = DEBUG
}