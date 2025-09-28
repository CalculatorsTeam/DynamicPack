package com.calculatorsteam.dynamicpack.platform

import com.calculatorsteam.dynamicpack.Constants
import com.calculatorsteam.dynamicpack.DynamicPackMod
import com.calculatorsteam.dynamicpack.client.DynamicPackModBase
import com.calculatorsteam.dynamicpack.client.config.ConfigScreenBuilder
import com.calculatorsteam.dynamicpack.client.config.NoYACLScreen
import com.calculatorsteam.dynamicpack.util.enums.Loader
import com.calculatorsteam.dynamicpack.util.log.Out
import net.minecraft.client.player.LocalPlayer
/*? if fabric {*/
import com.calculatorsteam.dynamicpack.platform.fabric.FabricDynamicPreLaunch
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.ClientPacketListener

class PlatformEntrypoint : ClientModInitializer {
    override fun onInitializeClient() {
        ClientPlayConnectionEvents.JOIN.register { clientPacketListener: ClientPacketListener, packetSender: PacketSender, minecraft: Minecraft ->
            FabricDynamicPreLaunch.getInstance().onWorldJoinForUpdateChecks(Minecraft.getInstance().player)
        }
    }
}
/*?} elif neoforge {*/
/*import net.neoforged.api.distmarker.Dist
import net.neoforged.fml.ModList
import net.neoforged.fml.ModLoadingContext
import net.neoforged.fml.common.Mod
import net.neoforged.fml.loading.FMLPaths
import net.neoforged.neoforge.client.gui.IConfigScreenFactory

@Mod(Constants.MOD_ID, dist = [Dist.CLIENT])
class PlatformEntrypoint : DynamicPackModBase() {

    init {
        instance = this

        val gameDir = FMLPaths.GAMEDIR.get().toFile()
        init(gameDir, Loader.NEO_FORGE)

        Out.println("DynamicPack loaded. Hello NeoForge world!")

        ModList.get().getModContainerById(Constants.MOD_ID).ifPresent { container ->
            val meta = container.modInfo
            val gitHash = meta.modProperties[Constants.MOD_ID + ":buildGitHash"]
            gitHash.let { Out.println("Build git hash: $it") }
        }

        ModLoadingContext.get().registerExtensionPoint(
            IConfigScreenFactory::class.java
        ) {
            IConfigScreenFactory { _, parent ->
                if (DynamicPackMod.instance.isModExists("yet_another_config_lib_v3")) {
                    ConfigScreenBuilder.create(parent)
                } else {
                    NoYACLScreen(parent)
                }
            }
        }
    }

    override fun isModExists(id: String): Boolean =
        ModList.get().isLoaded(id)

    fun onWorldJoin(localPlayer: LocalPlayer) {
        onWorldJoinForUpdateChecks(localPlayer)
    }

    companion object {
        @JvmStatic
        lateinit var instance: PlatformEntrypoint
            private set
    }
}
*//*?} elif forge {*/
/*import net.minecraftforge.client.ConfigScreenHandler
import net.minecraftforge.fml.ModList
import net.minecraftforge.fml.ModLoadingContext
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.loading.FMLPaths

@Mod(Constants.MOD_ID)
class PlatformEntrypoint() : DynamicPackModBase() {

    init {
        instance = this

        val gameDir = FMLPaths.GAMEDIR.get().toFile()
        init(gameDir, Loader.FORGE)

        Out.println("DynamicPack loaded. Hello Forge world!")

        ModList.get().getModContainerById(Constants.MOD_ID).ifPresent { container ->
            val meta = container.modInfo
            val gitHash = meta.modProperties[Constants.MOD_ID + ":buildGitHash"]
            gitHash.let { Out.println("Build git hash: $it") }
        }

        ModLoadingContext.get().registerExtensionPoint(
            ConfigScreenHandler.ConfigScreenFactory::class.java
        ) {
            ConfigScreenHandler.ConfigScreenFactory { _, parent ->
                if (DynamicPackMod.instance.isModExists("yet_another_config_lib_v3")) {
                    ConfigScreenBuilder.create(parent)
                } else {
                    NoYACLScreen(parent)
                }
            }
        }
    }

    override fun isModExists(id: String): Boolean =
        ModList.get().isLoaded(id)

    fun onWorldJoin(localPlayer: LocalPlayer) {
        onWorldJoinForUpdateChecks(localPlayer)
    }

    companion object {
        @JvmStatic
        lateinit var instance: PlatformEntrypoint
            private set
    }
}
*//*?}*/

