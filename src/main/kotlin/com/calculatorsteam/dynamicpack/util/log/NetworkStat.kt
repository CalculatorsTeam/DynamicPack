package com.calculatorsteam.dynamicpack.util.log

import com.calculatorsteam.dynamicpack.Constants
import java.io.IOException
import kotlin.time.Duration.Companion.seconds

/**
 * Network statistics: speed, totals, ETA.
 */
object NetworkStat {
    const val MAGIC_NO_STATISTIC: Long = -1

    private val debugCallLoopLog = LoopLog(1.seconds)
    private var millis: Long = 0
    private var bytes: Long = 0

    // used for multi-thread correction
    @JvmField
    var speedMultiplier: Long = 1 // костыль :-)

    /**
     * Run network task with counted bytes/time.
     */
    @Throws(IOException::class)
    fun <R> runNetworkTask(bytes: Long, runnable: () -> R): R {
        val start = System.currentTimeMillis()
        var exception: IOException? = null
        var result: R? = null
        try {
            result = runnable()
        } catch (e: IOException) {
            exception = e
        }
        val elapsed = System.currentTimeMillis() - start
        addLap(elapsed, bytes)
        if (exception != null) {
            throw exception
        }
        return result ?: throw IllegalStateException("Runnable returned null")
    }

    /**
     * @return speed in bytes/second
     */
    fun getSpeed(): Long {
        if (millis < 1000 || bytes == 0L) return MAGIC_NO_STATISTIC
        return (bytes / (millis / 1000f)).toLong() * speedMultiplier
    }

    /**
     * Add a lap to statistic
     */
    fun addLap(elapsed: Long, bytesRead: Long) {
        require(elapsed >= 0 && bytesRead >= 0) {
            "elapsed and bytesRead can't be negative!"
        }

        if (bytes > Constants.NETWORK_STAT_RESET_LIMIT) {
            bytes /= 3
            millis /= 3
            Out.debug("[NetworkStat] reset by divide all by 3")
        }
        millis += elapsed
        bytes += bytesRead

        debugCall()
    }

    /**
     * ETA in seconds
     */
    fun remainingETA(bytes: Long): Long {
        val speed = getSpeed()
        if (speed <= 0) return MAGIC_NO_STATISTIC
        return bytes / speed
    }

    private fun debugCall() {
        if (Constants.DEBUG && debugCallLoopLog.tick()) {
            Out.debug(
                "[NetworkStat] speed: ${Constants.speedToString(getSpeed())}; " +
                        "totalTime=$millis; totalBytes=$bytes"
            )
        }
    }
}