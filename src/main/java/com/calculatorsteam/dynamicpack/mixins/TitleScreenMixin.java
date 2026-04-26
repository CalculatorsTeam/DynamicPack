package com.calculatorsteam.dynamicpack.mixins;

import com.calculatorsteam.dynamicpack.Constants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenMixin {
    @Inject(
            at = @At("TAIL"),
            /*? if >=26.1 {*/
            method = "extractRenderState"
            /*?} else {*/
            /*method = "render"
            *//*?}*/
    )
    public void dynamicpack$extractRenderState(/*? if >=26.1 {*/net.minecraft.client.gui.GuiGraphicsExtractor/*?} else {*//*net.minecraft.client.gui.GuiGraphics*//*?}*/ context, int i, int j, float f, CallbackInfo ci) {
        if (Constants.DEBUG) {
            int k = Mth.ceil(Math.abs(Math.sin(((double) System.currentTimeMillis()) / 350d)) * 255.0f) << 24;
            /*? if >=26.1 {*/
            context.text(Minecraft.getInstance().font, Component.literal("DynamicPack mod is DEBUG").withStyle(ChatFormatting.BOLD).append(Component.literal(" use a release version for stable behavior").withStyle(ChatFormatting.YELLOW)), 2, context.guiHeight() - 20, 0xFF2222 | k);
            /*?} else {*/
            /*context.drawString(Minecraft.getInstance().font, Component.literal("DynamicPack mod is DEBUG").withStyle(ChatFormatting.BOLD).append(Component.literal(" use a release version for stable behavior").withStyle(ChatFormatting.YELLOW)), 2, context.guiHeight() - 20, 0xFF2222 | k);
            *//*?}*/
        }
    }
}

