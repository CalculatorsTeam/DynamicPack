package com.calculatorsteam.dynamicpack.sync

import java.nio.file.Path

/**
 * Sync pack info interface.
 */
interface SyncProgress {
    /**
     * Set current phase for sync operation
     */
    fun setPhase(phase: String)

    /**
     * Called while downloading data
     */
    fun downloading(name: String, percentage: Float)

    /**
     * Called when a file is deleted
     */
    fun deleted(name: Path)
}