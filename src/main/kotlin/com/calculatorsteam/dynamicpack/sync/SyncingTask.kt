package com.calculatorsteam.dynamicpack.sync

import com.calculatorsteam.dynamicpack.client.config.Config
import com.calculatorsteam.dynamicpack.DynamicPackMod
import com.calculatorsteam.dynamicpack.pack.DynamicResourcePack
import com.calculatorsteam.dynamicpack.util.log.Out

/**
 * Sync task.
 * Re-check all packs and update packs with update available
 */
object SyncingTask {
    private var isSyncing: Boolean = false

    private var syncingLog1: String = ""
    private var syncingLog2: String = ""
    private var syncingLog3: String = ""

    @JvmField
    var eta: Long = 0

    /** used in future for interrupt() download. Sets by Thread */
    @JvmField
    var currentRootSyncBuilder: SyncBuilder? = null

    @JvmField
    var currentPackName: String? = null

    /**
     * @param runnable to run
     * @throws RuntimeException if task already running (check isSyncing before call)
     */
    @JvmStatic
    fun launchTaskAsSyncing(runnable: () -> Unit) {
        if (isSyncing()) {
            throw RuntimeException("Failed to launchTaskAsSyncing. Other task currently working...")
        }

        val packs = DynamicPackMod.packsContainer
        setSyncing(true)
        packs.lockRescan()

        log("[SyncingTask] launchTaskAsSyncing start!")
        runnable()
        log("[SyncingTask] launchTaskAsSyncing end!")

        setSyncing(false)
        packs.unlockRescan()
        currentPackName = null
        currentRootSyncBuilder = null
    }

    @JvmStatic
    fun log(s: String) {
        Out.debug("[SyncingTask] log: $s")
        syncingLog1 = syncingLog2
        syncingLog2 = syncingLog3
        syncingLog3 = s
    }

    @JvmStatic
    fun clearLog() {
        syncingLog3 = ""
        syncingLog2 = ""
        syncingLog1 = ""
    }

    @JvmStatic
    fun getLogs(): String =
        "$syncingLog1\n$syncingLog2\n$syncingLog3"

    @JvmStatic
    fun setSyncing(syncing: Boolean) {
        isSyncing = syncing
    }

    @JvmStatic
    fun isSyncing(): Boolean = isSyncing

    @JvmStatic
    fun rootSyncBuilder(): SyncBuilder =
        object : SyncBuilder {
            private val builders: MutableSet<SyncBuilder> = mutableSetOf()
            private var updateAvailable: Boolean = false
            private var totalSize: Long = 0
            private var interrupted: Boolean = false

            @Throws(Exception::class)
            override fun init(ignoreCaches: Boolean) {
                clearLog()

                for (pack: DynamicResourcePack in DynamicPackMod.packsContainer.packsArray) {
                    if (interrupted) return

                    if (Config.getInstance().isUpdateOnlyEnabledPacks()) {
                        val enabled = DynamicPackMod.isResourcePackActive(pack)
                        if (!enabled) continue
                    }

                    val builder = pack.syncBuilder()
                    builder.init(ignoreCaches)
                    builders += builder

                    totalSize += builder.updateSize
                    if (builder.isUpdateAvailable) {
                        updateAvailable = true
                    }
                }
                Out.debug("[SyncingTask] rootSyncBuilder() totalSize=$totalSize updateAvailable=$updateAvailable")
            }

            override val isUpdateAvailable: Boolean
                get() = updateAvailable

            override val updateSize: Long
                get() = totalSize

            override val downloadedSize: Long
                get() = builders.sumOf { it.downloadedSize }

            @Throws(Exception::class)
            override fun doUpdate(progress: SyncProgress): Boolean {
                var reload = false
                for (syncBuilder in builders) {
                    if (interrupted) return false

                    if (syncBuilder.isUpdateAvailable) {
                        val rel = syncBuilder.doUpdate(progress)
                        if (rel) reload = true
                    }
                }
                return reload
            }

            override fun interrupt() {
                interrupted = true
                builders.forEach { it.interrupt() }
            }
        }
}