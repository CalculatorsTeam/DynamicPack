package com.calculatorsteam.dynamicpack.platform

import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.Renderable
import net.minecraft.client.gui.components.Tooltip
import net.minecraft.client.gui.components.events.GuiEventListener
import net.minecraft.client.gui.narration.NarratableEntry
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.packs.TransferableSelectionList
import net.minecraft.client.player.LocalPlayer
import net.minecraft.locale.Language
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.FormattedText
import net.minecraft.network.chat.Style
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.FormattedCharSequence
import java.net.URI

object VersionFunctions {
    private val client: Minecraft = Minecraft.getInstance()

    /**
     * Creates a vanilla Button with proper generics
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> createButton(
        text: Component,
        press: () -> Unit,
        w: Int, h: Int,
        x: Int, y: Int
    ): T where T : GuiEventListener, T : Renderable, T : NarratableEntry {
        return Button.builder(text) { press.invoke() }
            .size(w, h)
            .pos(x, y)
            .build() as T
    }

    /**
     * Rewritten from ModMenu (MIT).
     * Draw wrapped string with maximum lines.
     */
    fun drawWrappedString(
        matrices: GuiGraphics,
        stringIn: String,
        x: Int, y: Int,
        wrapWidth: Int,
        lines: Int,
        color: Int
    ) {
        var string = stringIn
        while (string.endsWith("\n")) {
            string = string.removeSuffix("\n")
        }
        val strings: List<FormattedText> = client.font.splitter.splitLines(Component.literal(string), wrapWidth, Style.EMPTY)
        for (i in strings.indices) {
            if (i >= lines) break
            var renderable: FormattedText = strings[i]
            if (i == lines - 1 && strings.size > lines) {
                renderable = FormattedText.composite(renderable, FormattedText.of("..."))
            }
            val line: FormattedCharSequence = Language.getInstance().getVisualOrder(renderable)
            var x1 = x
            if (client.font.isBidirectional) {
                val width = client.font.width(line)
                x1 += (wrapWidth - width)
            }
            matrices.drawString(client.font, line, x1, y + i * client.font.lineHeight, color, false)
        }
    }

    fun runAtUI(task: () -> Unit) {
        client.execute(task)
    }

    fun drawTexture(
        context: GuiGraphics,
        texture: ResourceLocation?,
        x: Int, y: Int,
        u: Float, v: Float,
        width: Int, height: Int,
        textureWidth: Int, textureHeight: Int
    ) {
        /*? if >=1.21.6 {*/
        if (texture != null) {
            RenderSystem.setShaderTexture(0, Minecraft.getInstance().textureManager.getTexture(texture).textureView);
            context.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, texture, x, y, u,  v, width, height, textureWidth, textureHeight)
        }
        /*?} else if >=1.21.5 {*/
        /*if (texture != null) {
            RenderSystem.setShaderTexture(0, Minecraft.getInstance().getTextureManager().getTexture(texture).getTexture());
            context.blit(net.minecraft.client.renderer.RenderType::guiTextured, texture, x, y, u,  v, width, height, textureWidth, textureHeight)
        }
        *//*?} else if >=1.21.2 {*/
        /*if (texture != null) {
            RenderSystem.setShaderTexture(0, texture)
            context.blit(net.minecraft.client.renderer.RenderType::guiTextured, texture, x, y, u,  v, width, height, textureWidth, textureHeight)
        }
        *//*?} else {*/
        /*if (texture != null) {
            RenderSystem.setShaderTexture(0, texture)
            context.blit(texture, x, y, 1, u, v, width, height, textureWidth, textureHeight)
        }
        *//*?}*/
    }

    fun renderBackground(screen: Screen, context: Any, mouseX: Int, mouseY: Int, delta: Float) {
        /*? if >=1.21 {*/
        screen.renderBackground(context as GuiGraphics, mouseX, mouseY, delta)
        /*?} else {*/
        /*screen.renderBackground(context as GuiGraphics)
        *//*?}*/
    }

    fun drawString(context: Any, font: Font, component: Component, x: Int, y: Int, color: Int) {
        (context as GuiGraphics).drawString(font, component, x, y, color)
    }

    fun drawCenteredString(context: Any, font: Font, title: Component, x: Int, y: Int, color: Int) {
        (context as GuiGraphics).drawCenteredString(font, title, x, y, color)
    }

    @JvmStatic
    fun blitSprite(context: Any, sprite: ResourceLocation , x: Int, y: Int, width: Int, height: Int) {
        /*? if >=1.21.6 {*/
        (context as GuiGraphics).blitSprite(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, sprite, x, y, width, height)
        /*?} else if >=1.21.2 {*/
        /*(context as GuiGraphics).blitSprite(net.minecraft.client.renderer.RenderType::guiTextured, sprite, x, y, width, height)
        *//*?} else if >=1.21 {*/
        /*(context as GuiGraphics).blitSprite(sprite, x, y, width, height)
        *//*?}*/
    }

    fun displayClientMessage(player: LocalPlayer?, chatComponent: Component) {
        /*? if >=1.21.2 {*/
        player?.displayClientMessage(chatComponent, false)
        /*?} else {*/
        /*player?.sendSystemMessage(chatComponent)
        *//*?}*/
    }

    fun setTooltip(button: Button, tooltip: Tooltip?) {
        /*? if >=1.21.6 {*/
        button.setTooltip(tooltip)
        /*?} else {*/
        /*button.tooltip = tooltip
        *//*?}*/
    }

    fun versionName(version: net.minecraft.WorldVersion): String {
        /*? if >=1.21.6 {*/
        return version.name()
        /*?} else {*/
        /*return version.name
        *//*?}*/
    }

    fun versionString(): String {
        return "1.21.8"
    }

    fun getToastManager():
        /*? if >=1.21.2 {*/
            net.minecraft.client.gui.components.toasts.ToastManager {
        return client.toastManager
        /*?} else {*/
            /*net.minecraft.client.gui.components.toasts.ToastComponent {
        return client.toasts
        *//*?}*/
    }

    fun getSystemToastId():
        /*? if >=1.20.4 {*/
            net.minecraft.client.gui.components.toasts.SystemToast.SystemToastId {
        return net.minecraft.client.gui.components.toasts.SystemToast.SystemToastId(5000)
        /*?} else {*/
            /*net.minecraft.client.gui.components.toasts.SystemToast.SystemToastIds {
        return net.minecraft.client.gui.components.toasts.SystemToast.SystemToastIds.NARRATOR_TOGGLE
        *//*?}*/
    }

    fun getHoverEventShowText(component: Component): net.minecraft.network.chat.HoverEvent {
        /*? if >=1.21.5 {*/
        return net.minecraft.network.chat.HoverEvent.ShowText(component)
        /*?} else {*/
        /*return net.minecraft.network.chat.HoverEvent(net.minecraft.network.chat.HoverEvent.Action.SHOW_TEXT, component)
        *//*?}*/
    }

    fun getClickEventOpenUrl(url: String): net.minecraft.network.chat.ClickEvent {
        /*? if >=1.21.5 {*/
        return net.minecraft.network.chat.ClickEvent.OpenUrl(URI.create(url))
        /*?} else {*/
        /*return net.minecraft.network.chat.ClickEvent(net.minecraft.network.chat.ClickEvent.Action.OPEN_URL, url)
        *//*?}*/
    }

    @JvmStatic
    fun maxScrollAmount(parent: TransferableSelectionList): Int {
        /*? if >=1.21.4 {*/
        return parent.maxScrollAmount()
        /*?} else {*/
        /*return parent.getMaxScroll()
        *//*?}*/
    }

    @JvmStatic
    fun applyModelViewMatrix() {
        /*? if <1.21.2 {*/
        /*RenderSystem.applyModelViewMatrix();
        *//*?}*/
    }

    @JvmStatic
    fun clear(int: Int) {
        /*? if >=1.21.5 {*/
        com.mojang.blaze3d.opengl.GlStateManager._clear(int)
        /*?} else if >=1.21.2 {*/
        /*RenderSystem.clear(int);
        *//*?} else {*/
        /*RenderSystem.clear(16640, net.minecraft.client.Minecraft.ON_OSX);
        *//*?}*/
    }

    @JvmStatic
    fun clearColor(red: Float, green: Float, blue: Float, alpha: Float) {
        /*? if >=1.21.5 {*/
        org.lwjgl.opengl.GL11.glClearColor(red, green, blue, alpha)
        /*?} else {*/
        /*RenderSystem.clearColor(red, green, blue, alpha)
        *//*?}*/
    }
}