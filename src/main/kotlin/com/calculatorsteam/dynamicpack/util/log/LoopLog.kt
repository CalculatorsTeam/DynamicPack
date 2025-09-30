package com.calculatorsteam.dynamicpack.util.log

import kotlin.time.Duration

/**
 * Millis timer based on [System.currentTimeMillis].
 */
class LoopLog(private val interval: Duration) {
    private var latest: Long = 0

    fun tick(): Boolean {
        val current = System.currentTimeMillis()
        return if (current - latest > interval.inWholeMilliseconds) {
            latest = current
            true
        } else {
            false
        }
    }
}