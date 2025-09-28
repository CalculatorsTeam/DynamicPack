package com.calculatorsteam.dynamicpack.util.log

import com.calculatorsteam.dynamicpack.Constants
import com.calculatorsteam.dynamicpack.util.enums.Loader
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.FileNotFoundException
import java.net.UnknownHostException

/**
 * Mod logger
 */
object Out {
    private val LOGGER: Logger = LogManager.getLogger("dynamicpack")
    private const val DEFAULT_PREFIX = "[DynamicPack] "

    @JvmField var ENABLE: Boolean = true
    @JvmField var USE_SOUT: Boolean = false
    private var prefix: String = ""

    @JvmStatic
    fun println(o: Any?) {
        if (!ENABLE) return
        if (USE_SOUT) {
            kotlin.io.println(prefix + o)
        } else {
            LOGGER.info("{}{}", prefix, o)
        }
    }

    @JvmStatic
    fun error(s: String, e: Throwable) {
        if (!ENABLE) return
        val stacktrace = isPrintErrorStackTrace(e)
        if (USE_SOUT) {
            System.err.println(prefix + s)
            if (stacktrace) {
                e.printStackTrace()
            }
        } else {
            if (stacktrace) {
                LOGGER.error("{}{}", prefix, s, e)
            } else {
                LOGGER.error("{}{}: {}", prefix, s, e.toString())
            }
        }
    }

    private fun isPrintErrorStackTrace(e: Throwable): Boolean =
        e !is FileNotFoundException && e !is UnknownHostException

    fun warn(s: String) {
        if (!ENABLE) return
        if (USE_SOUT) {
            kotlin.io.println(prefix + "WARN: " + s)
        } else {
            LOGGER.warn("{}{}", prefix, s)
        }
    }

    /**
     * Always enable! Ignore enable/disable
     */
    fun securityWarning(s: String) {
        if (USE_SOUT) {
            kotlin.io.println("[DynamicPack] $s")
            return
        }
        runCatching {
            LOGGER.warn("[DynamicPack] {}", s)
        }.onFailure {
            kotlin.io.println("[DynamicPack] $s")
        }
    }

    fun debug(s: String) {
        if (Constants.isDebugLogs()) {
            println("DEBUG: $s")
        }
    }

    /**
     * Always enable! Ignore enable/disable
     */
    fun securityStackTrace() {
        if (USE_SOUT) {
            kotlin.io.println("[DynamicPack] Stacktrace")
            Throwable("StackTrace printer").printStackTrace()
            return
        }
        LOGGER.error(
            "[DynamicPack] No error. This is stacktrace printer",
            Throwable("StackTrace printer")
        )
    }

    fun init() {
        if (Constants.isRelease()) {
            prefix = DEFAULT_PREFIX
        }
    }
}