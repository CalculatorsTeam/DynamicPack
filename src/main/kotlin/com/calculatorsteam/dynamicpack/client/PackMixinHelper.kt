package com.calculatorsteam.dynamicpack.client

import com.calculatorsteam.dynamicpack.DynamicPackMod

/**
 * Helper for mixin hooks into Minecraft lifecycle.
 */
object PackMixinHelper {

    @JvmStatic
    fun minecraftInitReturned() {
        DynamicPackMod.instance.minecraftInitialized()
    }

    @JvmStatic
    fun updatePacksMinecraftRequest() {
        DynamicPackMod.packsContainer.rescan()
    }
}