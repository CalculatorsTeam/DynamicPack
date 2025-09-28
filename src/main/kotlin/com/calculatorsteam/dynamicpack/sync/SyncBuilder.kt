package com.calculatorsteam.dynamicpack.sync

/**
 * SyncBuilder
 */
interface SyncBuilder {
    /**
     * Initialize a SyncBuilder, cache updateAvailable status, update size, etc...
     */
    @Throws(Exception::class)
    fun init(ignoreCaches: Boolean)

    /**
     * @return downloaded size
     */
    val downloadedSize: Long

    /**
     * @return cached in init() value
     */
    val isUpdateAvailable: Boolean

    /**
     * @return calculated and cached in init() value
     */
    val updateSize: Long

    /**
     * Update pack
     * @return is needed a reload
     */
    @Throws(Exception::class)
    fun doUpdate(progress: SyncProgress): Boolean

    /**
     * Stop update
     */
    fun interrupt()
}