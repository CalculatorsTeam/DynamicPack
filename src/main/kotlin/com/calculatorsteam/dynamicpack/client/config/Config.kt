package com.calculatorsteam.dynamicpack.client.config

import com.calculatorsteam.dynamicpack.Constants
import com.calculatorsteam.dynamicpack.pack.dynamicrepo.DynamicRepoSyncBuilder
import com.calculatorsteam.dynamicpack.util.PathUtil
import com.calculatorsteam.dynamicpack.util.log.FileLog
import com.calculatorsteam.dynamicpack.util.log.Out
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

/**
 * Global config for DynamicPack.
 */
class Config private constructor() {

    @Transient
    private var def: Boolean = false

    private var formatVersion: Int = 1 // do not touch

    private var networkBufferSize: Int = 1024
    fun getNetworkBufferSize(): Int = networkBufferSize
    fun setNetworkBufferSize(value: Int) {
        networkBufferSize = value
        updateStaticVariables(this)
    }

    private var networkMultithreadDownloadThreads: Int = 12
    fun getNetworkMultithreadDownloadThreads(): Int = networkMultithreadDownloadThreads
    fun setNetworkMultithreadDownloadThreads(value: Int) {
        networkMultithreadDownloadThreads = value
        updateStaticVariables(this)
    }

    private var logAllFilesChanges: Boolean = false
    fun isLogAllFilesChanges(): Boolean = logAllFilesChanges
    fun setLogAllFilesChanges(value: Boolean) {
        logAllFilesChanges = value
        updateStaticVariables(this)
    }

    private var autoUpdateAtLaunch: Boolean = true
    fun isAutoUpdateAtLaunch(): Boolean = autoUpdateAtLaunch
    fun setAutoUpdateAtLaunch(value: Boolean) {
        autoUpdateAtLaunch = value
    }

    private var updateOnlyEnabledPacks: Boolean = true
    fun isUpdateOnlyEnabledPacks(): Boolean = updateOnlyEnabledPacks
    fun setUpdateOnlyEnabledPacks(value: Boolean) {
        updateOnlyEnabledPacks = value
    }

    private var debugIgnoreHiddenFlagInContents: Boolean = false
    fun dynamicRepoIsIgnoreHiddenContentFlag(): Boolean = debugIgnoreHiddenFlagInContents
    fun setDebugIgnoreHiddenFlagInContents(value: Boolean) {
        debugIgnoreHiddenFlagInContents = value
    }

    private var debugLogs: Boolean = false
    fun isDebugLogs(): Boolean = debugLogs
    fun setDebugLogs(value: Boolean) {
        debugLogs = value
    }

    private fun checkAndValidateConfig() {
        var save = false

        if (networkBufferSize < 256) {
            networkBufferSize = 256
            save = true
            Out.warn("Config invalid 'networkBufferSize'. Sets to $networkBufferSize")
        }

        if (networkMultithreadDownloadThreads !in 1..<256) {
            networkMultithreadDownloadThreads = 8
            save = true
            Out.warn("Config invalid 'networkMultithreadDownloadThreads'. Sets to $networkMultithreadDownloadThreads")
        }

        if (save) save()
    }

    fun save() {
        if (def) throw RuntimeException("Can't save a DEF config!")
        try {
            val json = Constants.GSON.toJson(this)
            val file: Path = fileRef!!.toPath()
            PathUtil.delete(file)
            PathUtil.createDirsToFile(file)
            PathUtil.createFile(file)
            Files.writeString(file, json, StandardOpenOption.WRITE)
        } catch (e: Exception) {
            Out.error("Config save failed :(", e)
        }
    }

    companion object {
        @JvmField val DEF: Config = createDefConfig()
        private var instance: Config? = null
        internal var fileRef: File? = null

        private fun createDefConfig(): Config {
            val cfg = Config()
            cfg.def = true
            return cfg
        }

        @JvmStatic
        fun load(): Config {
            if (instance != null) throw RuntimeException("Config already loaded")

            val file = fileRef ?: throw RuntimeException("Config.fileRef not set before load()!")

            if (!file.exists()) {
                val cfg = Config()
                updateStaticVariables(cfg)
                instance = cfg
                return cfg
            }

            return try {
                val config = Constants.GSON.fromJson(
                    PathUtil.readString(file.toPath()), Config::class.java
                )
                updateStaticVariables(config)
                instance = config
                config
            } catch (e: Exception) {
                Out.error("Config load failed (return default config)", e)
                val fallback = Config()
                updateStaticVariables(fallback)
                instance = fallback
                fallback
            }
        }

        private fun updateStaticVariables(config: Config) {
            config.checkAndValidateConfig()
            Constants.URLS_BUFFER_SIZE = config.networkBufferSize
            Constants.DEBUG_LOGS = config.debugLogs
            DynamicRepoSyncBuilder.DOWNLOAD_THREADS_COUNT = config.networkMultithreadDownloadThreads
            FileLog.logAllChanges = config.logAllFilesChanges
        }

        @JvmStatic
        fun getInstance(): Config = instance ?: throw IllegalStateException("Config not initialized!")
    }
}