package com.calculatorsteam.dynamicpack.pack

import com.calculatorsteam.dynamicpack.DynamicPackMod
import com.calculatorsteam.dynamicpack.Constants
import com.calculatorsteam.dynamicpack.sync.SyncBuilder
import com.calculatorsteam.dynamicpack.sync.SyncProgress
import com.calculatorsteam.dynamicpack.sync.SyncingTask
import com.calculatorsteam.dynamicpack.util.*
import com.calculatorsteam.dynamicpack.util.LockUtils
import com.calculatorsteam.dynamicpack.util.log.Out
import com.calculatorsteam.dynamicpack.util.status.StatusChecker
import com.google.gson.JsonObject
import java.io.File
import java.io.IOException
import java.nio.file.Path

class DynamicResourcePack(
    private val location: File, // in resourcepack dir
    private val cachedJson: JsonObject // json of dynamicmcpack.json
) : AbstractPack {

    private val remote: Remote
    private val remoteTypeStr: String

    private var cachedUpdateAvailable: Boolean = false
    private var latestException: Exception? = null
    private val destroyListeners = mutableListOf<(DynamicResourcePack) -> Unit>()

    private var _isSyncing: Boolean = false
    private var destroyed: Boolean = false
    private var activeSyncBuilder: SyncBuilder? = null

    init {
        try {
            val remoteJson = cachedJson.getAsJsonObject("remote")
            remoteTypeStr = remoteJson.getAsJsonPrimitive("type").asString

            remote = Remote.REMOTES[remoteTypeStr]?.invoke()
                ?: throw RuntimeException("Unknown remote type: $remoteTypeStr")

            remote.init(this, remoteJson)
        } catch (e: Exception) {
            throw RuntimeException("Failed to parse 'remote' block", e)
        }
    }

    override val isSyncing: Boolean get() = _isSyncing

    /**
     * Return pack SyncBuilder with wrapped throws
     */
    fun syncBuilder(): SyncBuilder = object : SyncBuilder {
        private var builder: SyncBuilder? = null
        private var notUpdate = false

        private fun wrapThrowable(funBlock: () -> Unit) {
            wrapThrowableRet({ funBlock(); null }, null)
        }

        private fun <T> wrapThrowableRet(funBlock: () -> T, def: T): T {
            return try {
                funBlock()
            } catch (e: Exception) {
                _isSyncing = false
                setLatestException(e)
                notUpdate = true
                error("Error while doUpdate (or init) SyncBuilder", e)
                def
            }
        }

        override fun init(ignoreCaches: Boolean) {
            _isSyncing = true
            activeSyncBuilder = this
            wrapThrowable {
                checkNetwork()
                builder = remote.syncBuilder().apply { init(ignoreCaches) }
            }
            _isSyncing = false
            activeSyncBuilder = null
        }

        override val downloadedSize: Long
            get() = if (notUpdate) 0 else wrapThrowableRet({ builder?.downloadedSize ?: -1L }, -1L)

        override val isUpdateAvailable: Boolean
            get() = if (notUpdate) false else wrapThrowableRet({ builder?.isUpdateAvailable ?: false }, false)

        override val updateSize: Long
            get() = if (notUpdate) 0 else wrapThrowableRet({ builder?.updateSize ?: -1L }, -1L)

        override fun doUpdate(progress: SyncProgress): Boolean {
            if (notUpdate) return false

            SyncingTask.currentPackName = name
            return wrapThrowableRet({
                activeSyncBuilder = this
                _isSyncing = true
                val b = builder?.doUpdate(progress) ?: false
                try {
                    validateSafePackMinecraftMeta()
                    setLatestException(null)
                } catch (e2: Exception) {
                    error("Error while check safe pack meta", e2)
                    setLatestException(e2)
                } finally {
                    _isSyncing = false
                    activeSyncBuilder = null
                }
                b
            }, false)
        }

        override fun interrupt() {
            builder?.interrupt()
        }
    }

    fun saveClientFile() {
        try {
            PackUtil.openPackFileSystem(location, LockUtils.createFileFinalizer(location)) {
                saveClientFile(it)
            }
        } catch (e: Exception) {
            throw RuntimeException("saveClientFile failed.", e)
        }
    }

    fun saveClientFile(packRoot: Path) {
        PathUtil.nioWriteText(
            packRoot.resolve(Constants.CLIENT_FILE),
            JsonUtil.toString(packJson)
        )
    }

    fun isNetworkBlocked(): Boolean = StatusChecker.isBlockUpdating(remoteTypeStr)

    private fun checkNetwork() {
        if (isNetworkBlocked()) {
            throw SecurityException("Network is blocked for remote_type=$remoteTypeStr current version of mod not safe. Update mod!")
        }
    }

    fun isZip(): Boolean =
        !location.isDirectory && location.name.lowercase().endsWith(".zip")

    fun isDestroyed(): Boolean = destroyed

    fun getRemote(): Remote = remote
    fun getLocation(): File = location
    val name: String get() = location.name
    val minecraftId: String get() = "file/${location.name}"
    val packJson: JsonObject get() = cachedJson
    val currentJson: JsonObject get() = cachedJson.getAsJsonObject("current")
    fun getRemoteType(): String = remoteTypeStr

    fun setLatestException(e: Exception?) {
        debug("latestException=$e")
        latestException = e
    }

    fun getLatestException(): Exception? = latestException

    fun getLatestUpdated(): Long {
        return try {
            cachedJson.getAsJsonObject("current").get("latest_updated").asLong
        } catch (e: Exception) {
            -1
        }
    }

    fun updateJsonLatestUpdate() {
        cachedJson.getAsJsonObject("current")
            .addProperty("latest_updated", System.currentTimeMillis() / 1000)
    }

    @Throws(IOException::class)
    fun checkIsUpdateAvailable(): Boolean {
        checkNetwork()
        return remote.checkUpdateAvailable().also { cachedUpdateAvailable = it }
    }

    fun getCachedUpdateAvailableStatus(): Boolean = cachedUpdateAvailable

    @Throws(Exception::class)
    private fun validateSafePackMinecraftMeta() {
        PackUtil.openPackFileSystem(location) { path ->
            val mcmeta = path.resolve(Constants.MINECRAFT_META)
            var safe = PathUtil.isPathFileExists(mcmeta)
            if (safe) {
                safe = try {
                    checkMinecraftMetaIsValid(PathUtil.readString(mcmeta))
                } catch (_: IOException) {
                    false
                }
            }
            if (!safe) {
                PathUtil.nioWriteText(mcmeta, Constants.UNKNOWN_PACK_MCMETA)
            }
        }
    }

    private fun checkMinecraftMetaIsValid(s: String): Boolean {
        return try {
            DynamicPackMod.instance.checkResourcePackMetaValid(s)
        } catch (e: Exception) {
            error("Error while check meta valid.", e)
            false
        }
    }

    fun addDestroyListener(runnable: (DynamicResourcePack) -> Unit) {
        destroyListeners += runnable
    }

    fun removeDestroyListener(runnable: (DynamicResourcePack) -> Unit) {
        destroyListeners -= runnable
    }

    fun flashback(oldestPack: DynamicResourcePack?) {
        if (oldestPack == null) return
        oldestPack.markAsDestroyed(this)

        if (latestException == null) {
            latestException = oldestPack.latestException
        }
    }

    private fun markAsDestroyed(heirPack: DynamicResourcePack) {
        destroyListeners.toTypedArray().forEach { it(heirPack) }
        destroyListeners.clear()
        destroyed = true
    }

    private fun interrupt() {
        activeSyncBuilder?.interrupt()
    }

    fun debug(s: String) {
        Out.debug("{$name} $s")
    }

    fun error(s: String, e: Throwable) {
        Out.error("{$name} $s", e)
    }

    fun warn(s: String) {
        Out.warn("{$name} $s")
    }

    fun println(s: String) {
        Out.println("{$name} $s")
    }
}