package com.calculatorsteam.dynamicpack.util

import com.calculatorsteam.dynamicpack.util.log.NetworkStat
import org.jetbrains.annotations.ApiStatus
import java.util.function.LongConsumer

open class UrlsController : LongConsumer {
    companion object {
        const val MAGIC_DEV_BY_ZERO = -1000f
        const val MAGIC_OVERMAXING = 101f

        @JvmStatic
        fun isInterrupted(controller: UrlsController?): Boolean =
            controller?.isInterrupted() == true

        @JvmStatic
        fun updateCurrent(controller: UrlsController?, current: Long) {
            controller?.updateCurrent(current)
        }
    }

    private var interrupted: Boolean = false
    private var max: Long = 0
    private var latest: Long = 0

    fun updateCurrent(value: Long) {
        latest = value
        onUpdate(this)
    }

    fun updateMax(value: Long) {
        max = value.coerceAtLeast(max)
    }

    /**
     * @deprecated Override a LongConsumer for back compatibility
     */
    @Deprecated("Use updateCurrent/updateMax directly", ReplaceWith("updateCurrent(value)"))
    override fun accept(value: Long) {
        updateCurrent(value)
        updateMax(value)
    }

    fun getPercentage(): Float = when {
        max < latest -> MAGIC_OVERMAXING
        max == 0L -> MAGIC_DEV_BY_ZERO
        latest == max -> 100f
        else -> (latest.toFloat() * 100f) / max.toFloat()
    }

    /**
     * Remaining time in seconds
     */
    fun getRemaining(): Long = NetworkStat.remainingETA(max - latest)

    fun getLatest(): Long = latest

    open fun isInterrupted(): Boolean = false

    fun interrupt() {
        interrupted = false
    }

    @ApiStatus.OverrideOnly
    open fun onUpdate(it: UrlsController) {
        // override it in subclasses
    }
}