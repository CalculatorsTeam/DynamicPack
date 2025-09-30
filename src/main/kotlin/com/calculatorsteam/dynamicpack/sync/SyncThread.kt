package com.calculatorsteam.dynamicpack.sync

import com.calculatorsteam.dynamicpack.DynamicPackMod
import com.calculatorsteam.dynamicpack.pack.DynamicResourcePack
import com.calculatorsteam.dynamicpack.util.log.LoopLog
import com.calculatorsteam.dynamicpack.util.log.NetworkStat
import com.calculatorsteam.dynamicpack.util.log.Out
import java.nio.file.Path
import kotlin.time.Duration.Companion.seconds

class SyncThread @JvmOverloads constructor(
    name: String,
    private val packSpecify: DynamicResourcePack? = null
) : Thread(name) {

    private var syncBuilder: SyncBuilder? = null
    private val etaLoopLog = LoopLog(1.seconds)

    override fun run() {
        Out.debug("[SyncThread] thread started")

        if (SyncingTask.isSyncing()) {
            Out.warn("Shutting down SyncThread because other thread already work")
            return
        }

        SyncingTask.launchTaskAsSyncing {
            try {
                SyncingTask.currentRootSyncBuilder =
                    (packSpecify?.syncBuilder() ?: SyncingTask.rootSyncBuilder())
                        .also { syncBuilder = it }

                syncBuilder?.init(true)

                if (syncBuilder?.isUpdateAvailable == true) {
                    val reloadRequired = syncBuilder!!.doUpdate(createSyncProgress())
                    if (reloadRequired) {
                        DynamicPackMod.instance.needResourcesReload()
                    }
                }
            } catch (e: Exception) {
                Out.error("Error while SyncThread...", e)
            } finally {
                SyncingTask.currentRootSyncBuilder = null
            }
        }
    }

    private fun createSyncProgress(): SyncProgress =
        object : SyncProgress {
            override fun setPhase(phase: String) {
                Out.debug("Phase: $phase")
                SyncingTask.log(phase)
            }

            override fun downloading(name: String, percentage: Float) {
                val sb = syncBuilder ?: return
                val remainsBytes = sb.updateSize - sb.downloadedSize
                val eta = NetworkStat.remainingETA(remainsBytes).also { SyncingTask.eta = it }
                if (etaLoopLog.tick()) {
                    Out.debug("(${SyncingTask.currentPackName}) ETA=${eta}s")
                }
            }

            override fun deleted(name: Path) {
                Out.debug("Deleted: $name")
                SyncingTask.log(name.fileName.toString())
            }
        }
}