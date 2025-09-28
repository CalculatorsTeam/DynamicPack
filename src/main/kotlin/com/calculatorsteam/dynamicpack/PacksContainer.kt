package com.calculatorsteam.dynamicpack

import com.calculatorsteam.dynamicpack.pack.DynamicResourcePack
import com.calculatorsteam.dynamicpack.util.JsonUtil
import com.calculatorsteam.dynamicpack.util.JsonUtil.optInt
import com.calculatorsteam.dynamicpack.util.PackUtil
import com.calculatorsteam.dynamicpack.util.PathUtil
import com.calculatorsteam.dynamicpack.util.log.Out
import com.calculatorsteam.dynamicpack.util.exception.FailedOpenPackFileSystemException
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

/**
 * Container for supported packs
 */
class PacksContainer(private val directoryToCheck: File) {

    private var rescanPacksBlocked: Boolean = false
    private var isPacksScanning: Boolean = false

    /** Current dynamic packs by filename */
    private val packs: MutableMap<String, DynamicResourcePack> = mutableMapOf()

    fun lockRescan() {
        rescanPacksBlocked = true
    }

    fun unlockRescan() {
        rescanPacksBlocked = false
    }

    fun rescan() {
        if (isPacksScanning) {
            Out.warn("Already in scanning!")
            return
        }
        if (rescanPacksBlocked) {
            Out.warn("Rescan blocked! maybe currently syncing")
            return
        }

        isPacksScanning = true
        val forDelete = packs.keys.toMutableList()

        PathUtil.listFiles(directoryToCheck)?.forEach { packFile ->
            val currentDynamicPack = DynamicPackMod.getDynamicPackByMinecraftName("file/${packFile.name}")
            if (currentDynamicPack?.isSyncing == true) {
                Out.warn("WARNING: Found a pack that is now synchronizing. skipping this pack")
                return@forEach
            }

            try {
                PackUtil.openPackFileSystem(packFile) { packPath ->
                    val clientFile = packPath.resolve(Constants.CLIENT_FILE)
                    if (Files.exists(clientFile)) {
                        Out.println("+ Pack ${packFile.name} supported by mod!")
                        processPack(packFile, clientFile)
                        forDelete.remove(packFile.name)
                    } else {
                        Out.println("- Pack ${packFile.name} not supported by mod.")
                    }
                }
            } catch (e: Exception) {
                if (e is FailedOpenPackFileSystemException) {
                    Out.warn("Error while processing pack ${packFile.name}: ${e.message}")
                } else {
                    Out.error("Error while processing pack: ${packFile.name}", e)
                }
            }
        }

        forDelete.forEach { s ->
            Out.println("Pack $s no longer exist or no longer support DynamicPack!")
            packs.remove(s)
        }

        isPacksScanning = false
    }

    @Throws(Exception::class)
    private fun processPack(location: File, clientFile: Path) {
        val json = JsonUtil.readJson(clientFile)
        val formatVersion = json.optInt("formatVersion")
        val oldestPack = packs[location.name]

        if (formatVersion == 1) {
            val pack = DynamicResourcePack(location, json).apply {
                if (oldestPack != null) {
                    flashback(oldestPack)
                }
            }
            packs[location.name] = pack
        } else {
            throw RuntimeException("Unsupported formatVersion for pack ${location.name}: $formatVersion")
        }
    }

    val packsArray: Array<DynamicResourcePack>
        get() = packs.values.toTypedArray()

    fun getByFileName(filename: String): DynamicResourcePack? = packs[filename]
}