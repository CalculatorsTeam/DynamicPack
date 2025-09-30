package com.calculatorsteam.dynamicpack.mixins;

import com.calculatorsteam.dynamicpack.client.PackMixinHelper;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PackSelectionScreen.class)
public class ResourcePackScreenMixin {
    /**
     * For auto-rescan packs
     */
    @Inject(at = @At("RETURN"), method = "populateLists")
    private void dynamicpack$updatePackLists(CallbackInfo ci) {
        PackMixinHelper.updatePacksMinecraftRequest();
    }
}

