package com.calculatorsteam.dynamicpack.pack

import com.calculatorsteam.dynamicpack.pack.dynamicrepo.DynamicRepoRemote
import com.calculatorsteam.dynamicpack.sync.SyncBuilder
import com.google.gson.JsonObject
import org.jetbrains.annotations.ApiStatus
import java.io.IOException

/**
 * Abstract remote of pack
 */
abstract class Remote {

    /**
     * Init this remote object and associate with pack
     * @param pack parent
     * @param remote root.remote
     */
    abstract fun init(pack: DynamicResourcePack, remote: JsonObject)

    abstract fun syncBuilder(): SyncBuilder

    @Throws(IOException::class)
    abstract fun checkUpdateAvailable(): Boolean

    @ApiStatus.OverrideOnly
    open fun interrupt() {
        // default empty implementation
    }

    companion object {
        private var initialized = false

        @JvmField
        val REMOTES: MutableMap<String, () -> Remote> = HashMap()

        @JvmStatic
        fun initRemoteTypes() {
            if (initialized) return
            initialized = true
            REMOTES["modrinth"] = { ModrinthRemote() }
            REMOTES["dynamic_repo"] = { DynamicRepoRemote() }
        }
    }
}