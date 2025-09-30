package com.calculatorsteam.dynamicpack.client

import com.calculatorsteam.dynamicpack.client.config.Config
import com.calculatorsteam.dynamicpack.DynamicPackMod
import com.calculatorsteam.dynamicpack.util.status.StatusChecker
import com.calculatorsteam.dynamicpack.sync.SyncBuilder
import com.calculatorsteam.dynamicpack.sync.SyncProgress
import com.calculatorsteam.dynamicpack.sync.SyncingTask
import com.calculatorsteam.dynamicpack.util.log.LoopLog
import com.calculatorsteam.dynamicpack.util.log.NetworkStat
import com.calculatorsteam.dynamicpack.util.log.Out
import java.nio.file.Path
import kotlin.time.Duration.Companion.seconds

/**
 * Syncing on game launch
 */
class GameStartSyncing : Thread("GameStartSyncingThread") {

    companion object {
        private const val MAX_LOCK_MS = 1000 * 15
    }

    private var updateStartTime: Long = 0L   // thread started time ms
    private var lockStartTime: Long = 0L     // lock started time ms

    /** locking resources loading (firstly true!) */
    var lockResourcesLoading: Boolean = true
        private set

    var syncBuilder: SyncBuilder? = null
    val etaLoopLog = LoopLog(1.seconds)

    init {
        if (!Config.getInstance().isAutoUpdateAtLaunch()) {
            Out.warn("Auto-update at launch disabled by config.")
            unlock()
        }
    }

    /**
     * Starting syncing... (thread entrypoint)
     */
    override fun run() {
        if (!Config.getInstance().isAutoUpdateAtLaunch()) {
            Out.warn("Thread launched with isAutoUpdateAtLaunch=false; return")
            return
        }

        updateStartTime = System.currentTimeMillis()
        Out.debug("[GameStartSyncing] thread started")

        SyncingTask.launchTaskAsSyncing {
            try {
                StatusChecker.check() // check status
                SyncingTask.currentRootSyncBuilder = SyncingTask.rootSyncBuilder().also { builder ->
                    syncBuilder = builder
                    builder.init(ignoreCaches = false)

                    if (builder.isUpdateAvailable) {
                        val reloadRequired = builder.doUpdate(createSyncProgress())
                        if (!lockResourcesLoading && reloadRequired) {
                            DynamicPackMod.instance.needResourcesReload()
                        }
                    }
                }
            } catch (e: Exception) {
                Out.error("Error while GameStartSyncing...", e)
            } finally {
                unlock() // unlock main thread!
                SyncingTask.currentRootSyncBuilder = null
            }
        }
    }

    private fun createSyncProgress(): SyncProgress = object : SyncProgress {
        override fun setPhase(phase: String) {
            Out.debug("Phase: $phase")
            SyncingTask.log(phase)
        }

        override fun downloading(name: String, percentage: Float) {
            syncBuilder?.let { builder ->
                val remainsBytes = builder.updateSize - builder.downloadedSize
                val eta = NetworkStat.remainingETA(remainsBytes).also { SyncingTask.eta = it }

                if (etaLoopLog.tick()) {
                    Out.debug("(${SyncingTask.currentPackName}) ETA=${eta}s")
                }

                // if locked and updating > 3 seconds
                if (updateTime > 3000 && isLocked()) {
                    val shouldUnlock = eta * 1000 > (untilForceUnlock / 1.5f)
                    if (shouldUnlock) {
                        Out.debug("[GameStartSyncing] ETA ${eta}s. Unlocking main thread...")
                        unlock()
                    }
                }
            }
        }

        override fun deleted(name: Path) {
            Out.debug("Deleted: $name")
            SyncingTask.log("Delete: ${name.fileName}")
        }
    }

    // если конфликт модов → переопределить
    fun isLockSupported(): Boolean = true

    fun lockedTick(): Boolean {
        if (lockTime > MAX_LOCK_MS) {
            Out.warn("Main thread unlocked forcibly because lock time >= 15s")
            return false
        }
        return true
    }

    val lockTime: Long
        get() = System.currentTimeMillis() - lockStartTime

    val updateTime: Long
        get() = System.currentTimeMillis() - updateStartTime

    val untilForceUnlock: Long
        get() = MAX_LOCK_MS - lockTime

    fun endGameLocking() {
        Out.println("Main thread locked for ${lockTime / 1000} seconds")
    }

    fun startGameLocking() {
        Out.println("Main thread locked by DynamicPack for updating resource packs...")
        lockStartTime = System.currentTimeMillis()
    }

    fun isLockStarted(): Boolean = lockStartTime != 0L
    fun isLocked(): Boolean = isLockStarted() && lockResourcesLoading

    private fun unlock() {
        lockResourcesLoading = false
    }

    val percentage: Double
        get() = syncBuilder?.let { b ->
            if (b.downloadedSize > 0)
                (b.downloadedSize.toDouble() / b.updateSize.toDouble()) * 100.0
            else 0.0
        } ?: 0.0
}