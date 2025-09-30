package com.calculatorsteam.dynamicpack

import com.calculatorsteam.dynamicpack.client.GameStartSyncing
import com.calculatorsteam.dynamicpack.client.config.Config
import com.calculatorsteam.dynamicpack.pack.DynamicResourcePack
import com.calculatorsteam.dynamicpack.pack.Remote
import com.calculatorsteam.dynamicpack.util.enums.Loader
import com.calculatorsteam.dynamicpack.util.log.Out
import org.jetbrains.annotations.ApiStatus
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files

/**
 * Base mod entry, singleton instance kept in [instance].
 */
abstract class DynamicPackMod protected constructor() {

    private lateinit var loader: Loader
    private lateinit var gameDir: File          // .minecraft
    private lateinit var configDir: File        // *gamedir*/config/dynamicpack
    private lateinit var configFile: File       // *configDir*/config.json
    private var _config: Config? = null
    private lateinit var packsContainer: PacksContainer
    private lateinit var gameStartSyncing: GameStartSyncing
    private var minecraftInitialized: Boolean = false

    companion object {
        private var INSTANCE: DynamicPackMod? = null

        @JvmStatic
        val instance: DynamicPackMod
            get() = INSTANCE ?: throw IllegalStateException("Mod not initialized")

        @JvmStatic
        val config: Config
            get() = instance._config ?: error("Config not initialized!")

        @JvmStatic
        @ApiStatus.AvailableSince("1.0.30")
        @Throws(Exception::class)
        fun addAllowedHosts(host: String, requester: Any) {
            Constants.addAllowedHosts(host, requester)
        }

        @JvmStatic
        fun isNameIsDynamic(name: String): Boolean =
            instance.packsContainer.getByFileName(name.removePrefix("file/")) != null

        @JvmStatic
        fun getDynamicPackByMinecraftName(name: String): DynamicResourcePack? {
            return instance.packsContainer.getByFileName(name.removePrefix("file/"))
        }

        @JvmStatic
        fun isResourcePackActive(pack: DynamicResourcePack): Boolean {
            val optionsFile = File(instance.gameDir, "options.txt")
            val lines: List<String> = try {
                Files.readAllLines(optionsFile.toPath(), StandardCharsets.UTF_8)
            } catch (e: Exception) {
                Out.println("options.txt not exists or failed to parse.. isResourcePackActive => false.")
                return false
            }

            return lines.any { line ->
                line.startsWith("resourcePacks:") && line.contains(pack.minecraftId)
            }
        }

        @JvmStatic val gameDir: File get() = instance.gameDir
        @JvmStatic val packsContainer: PacksContainer get() = instance.packsContainer
        @JvmStatic val loader: Loader get() = instance.loader
        @JvmStatic val gameStartSyncing: GameStartSyncing get() = instance.gameStartSyncing
        @JvmStatic val configDir: File get() = instance.configDir
        @JvmStatic val configFile: File get() = instance.configFile
    }

    protected var manuallySyncThreadCounter: Int = 0

    /**
     * Основная инициализация мода.
     */
    open fun init(gameDir: File, loader: Loader) {
        if (INSTANCE != null) throw RuntimeException("Already initialized!")
        INSTANCE = this
        this.gameDir = gameDir
        this.loader = loader

        // *gamedir*/resourcepacks
        val resourcePacks = File(gameDir, "resourcepacks")
        resourcePacks.mkdirs()

        this.configDir = File(gameDir, "config/dynamicpack").apply { mkdirs() }
        this.configFile = File(configDir, "config.json")

        // Передаём путь внутрь Config перед загрузкой!
        Config.fileRef = this.configFile
        val loadedConfig = Config.load()
        this._config = loadedConfig

        Remote.initRemoteTypes()
        Out.init()
        Out.println("Mod version: ${Constants.VERSION_NAME} build: ${Constants.VERSION_BUILD}")

        this.packsContainer = PacksContainer(resourcePacks)
        this.packsContainer.rescan()
        this.gameStartSyncing = GameStartSyncing()

        if (loadedConfig.isAutoUpdateAtLaunch()) {
            this.gameStartSyncing.start()
        }
    }

    // == ABSTRACT API ==
    abstract fun isModExists(id: String): Boolean
    /** Manually re-sync all supported packs */
    abstract fun startManuallySync()
    abstract fun startManuallySync(pack: DynamicResourcePack)
    abstract fun needResourcesReload()
    abstract fun getCurrentGameVersion(): String
    @Throws(Exception::class)
    abstract fun checkResourcePackMetaValid(s: String): Boolean
    // == END ABSTRACT ==

    fun minecraftInitialized() {
        this.minecraftInitialized = true
    }

    fun isMinecraftInitialized(): Boolean = minecraftInitialized
}