/*? if fabric {*/
package com.calculatorsteam.dynamicpack.platform.fabric

import com.calculatorsteam.dynamicpack.Constants
import com.calculatorsteam.dynamicpack.client.DynamicPackModBase
import com.calculatorsteam.dynamicpack.util.enums.Loader
import com.calculatorsteam.dynamicpack.util.log.Out
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint

/**
 * Fabric impl for DynamicPack mod
 */
class FabricDynamicPreLaunch : DynamicPackModBase(), PreLaunchEntrypoint {

    override fun onPreLaunch() {
        instance = this
        Out.println("DynamicPack loaded. Hello fabric world!")

        val gameDir = FabricLoader.getInstance().gameDir.toFile()
        init(gameDir, Loader.FABRIC)

        val container = FabricLoader.getInstance().getModContainer(Constants.MOD_ID).get()
        val buildHash = container.metadata.getCustomValue(Constants.MOD_ID + ":buildGitHash")?.asString
        if (buildHash != null) {
            Out.println("Build git hash: $buildHash")
        }
    }

    override fun isModExists(id: String): Boolean =
        FabricLoader.getInstance().isModLoaded(id)

    companion object {
        private var instance: FabricDynamicPreLaunch? = null
        @JvmStatic
        fun getInstance(): FabricDynamicPreLaunch =
            instance ?: throw IllegalStateException("FabricDynamicPreLaunch not initialized yet")
    }
}
/*?}*/