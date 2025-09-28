package com.calculatorsteam.dynamicpack.util.status

import com.calculatorsteam.dynamicpack.Constants
import com.calculatorsteam.dynamicpack.DynamicPackMod
import com.calculatorsteam.dynamicpack.util.JsonUtil.getLong
import com.calculatorsteam.dynamicpack.util.JsonUtil.fromString
import com.calculatorsteam.dynamicpack.util.enums.Loader
import com.calculatorsteam.dynamicpack.util.log.Out
import com.calculatorsteam.dynamicpack.util.Urls
import com.google.gson.JsonObject

/**
 * Check status from developer.
 */
object StatusChecker {
    private const val URL = "https://calculatorsteam.github.io/DynamicPack/dynamicpack.status.v1.json"

    private var isUpdateAvailable: Boolean = false
    private var isFormatActual: Boolean = true
    private var isSafe: Boolean = true
    private var isChecked: Boolean = false

    @JvmStatic
    fun check() {
        if (isChecked) return

        Out.println("Checking status...")
        try {
            val s = Urls.parseTextContent(URL, 1024 * 512) ?: return
            val j: JsonObject = fromString(s)
            val platformKey = getLatestKeyForPlatform(DynamicPackMod.loader)
            val lat = j.getAsJsonObject(platformKey)

            isUpdateAvailable = lat.getLong("build") > Constants.VERSION_BUILD
            isSafe = lat.getLong("safe") <= Constants.VERSION_BUILD
            isFormatActual = lat.getLong("format") <= Constants.VERSION_BUILD

            isChecked = true
            Out.println(
                "Status checked! platformKey=$platformKey, " +
                        "isSafe=$isSafe, isFormatActual=$isFormatActual, isUpdateAvailable=$isUpdateAvailable"
            )
        } catch (e: Exception) {
            Out.error("Error while checking status...", e)
        }
    }

    private fun getLatestKeyForPlatform(loader: Loader?): String = when (loader) {
        null, Loader.UNKNOWN -> "latest_version"
        Loader.FABRIC -> "latest_version_fabric"
        Loader.FORGE -> "latest_version_forge"
        Loader.NEO_FORGE -> "latest_version_neoforge"
    }

    @JvmStatic
    fun isBlockUpdating(remoteType: String): Boolean =
        if (remoteType == "modrinth") false else !isSafe()

    @JvmStatic
    fun isModUpdateAvailable(): Boolean = isUpdateAvailable

    @JvmStatic
    fun isSafe(): Boolean = isSafe

    @JvmStatic
    fun isFormatActual(): Boolean = isFormatActual

    @JvmStatic
    fun isChecked(): Boolean = isChecked
}