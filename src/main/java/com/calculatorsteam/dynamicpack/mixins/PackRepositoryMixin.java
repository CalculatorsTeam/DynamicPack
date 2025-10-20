package com.calculatorsteam.dynamicpack.mixins;

import com.calculatorsteam.dynamicpack.DynamicPackMod;
import com.calculatorsteam.dynamicpack.client.GameStartSyncing;
import com.calculatorsteam.dynamicpack.platform.VersionFunctions;
import com.calculatorsteam.dynamicpack.util.log.Out;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.repository.PackRepository;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PackRepository.class)
public class PackRepositoryMixin {

    /**
     * For freeze resource pack loading (only in game starting) until DynamicPack update it
     */
    @Inject(at = @At("HEAD"), method = "reload")
    private void dynamicpack$reload(CallbackInfo ci) {
        // do nothing if minecraft initialized
        if (DynamicPackMod.getInstance().isMinecraftInitialized()) {
            return;
        }

        Minecraft client = Minecraft.getInstance();
        GameStartSyncing syncing = DynamicPackMod.getGameStartSyncing();
        if (!syncing.isLockSupported()) {
            return;
        }

        if (syncing.getLockResourcesLoading()) {
            syncing.startGameLocking();
            while (!client.getWindow().shouldClose() && syncing.getLockResourcesLoading()) {
                if (!syncing.lockedTick()) {
                    break;
                }

                try {
                    VersionFunctions.applyModelViewMatrix();
                    VersionFunctions.clearColor(0.074f + (((float)syncing.getPercentage() / 100f)), 0.04f, (float) (0.24f + (Math.sin(System.currentTimeMillis() / 300f) / 2)), 1f);
                    VersionFunctions.clear(16640);
                    GLFW.glfwSwapBuffers(client.getWindow()/*? if >=1.21.9 {*/.handle()/*?} else {*//*.getWindow()*//*?}*/);

                } catch (Exception e) {
                    Out.error("Error while manipulations with OpenGL", e);
                }

                try {
                    Thread.sleep(50);
                } catch (InterruptedException ignored) {
                }
            }
            syncing.endGameLocking();
        }
    }
}

