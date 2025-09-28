package com.calculatorsteam.dynamicpack.client.config;

import com.calculatorsteam.dynamicpack.platform.VersionFunctions
import net.minecraft.ChatFormatting
import net.minecraft.Util
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.util.FormattedCharSequence
import net.minecraft.util.Mth
import java.net.URI

/**
 * Screen displayed if YACL (YetAnotherConfigLib) is not found.
 */
open class NoYACLScreen(private val parent: Screen) : Screen(Component.translatable("dynamicpack.screen.config.no_yacl.title")) {

    private val titleSequence: FormattedCharSequence =
        Component.translatable("dynamicpack.screen.config.no_yacl.title")
            .withStyle(ChatFormatting.BOLD)
            .visualOrderText

    private val unwrappedText: Component =
        Component.translatable(
            "dynamicpack.screen.config.no_yacl.description",
            Component.literal("YetAnotherConfigLib").withStyle {
                it.withClickEvent(VersionFunctions.getClickEventOpenUrl("https://modrinth.com/mod/yacl"))
                    .applyFormats(ChatFormatting.BLUE, ChatFormatting.UNDERLINE)
            },
        )

    private lateinit var wrappedText: List<FormattedCharSequence>

    override fun init() {
        wrappedText = font.split(unwrappedText, width - 50)

        addRenderableWidget(
            VersionFunctions.createButton<Button>(
                CommonComponents.GUI_BACK,
                {
                    minecraft?.setScreen(parent)
                },
                150, 20,
                (width - 150) / 2,
                Mth.clamp(90 + wrappedText.size * 9 + 12, height / 6 + 96, height - 24)
            )
        )
    }

    override fun render(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        /*? if >=1.21 {*/
        super.render(context, mouseX, mouseY, delta)
        /*?} else {*/
        /*VersionFunctions.renderBackground(this, context, mouseX, mouseY, delta);
        *//*?}*/

        context.drawCenteredString(font, titleSequence, width / 2, 70, -1)

        var y = 90
        for (line in wrappedText) {
            context.drawCenteredString(font, line, width / 2, y, -1)
            y += font.lineHeight
        }
        //? if < 1.21
        /*super.render(context, mouseX, mouseY, delta)*/
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true
        }

        val style = getStyle(mouseX.toInt(), mouseY.toInt()) ?: return false

        if (minecraft?.player == null) {
            val click = style.clickEvent ?: return false
            /*? if >=1.21.5 {*/
            if (click is ClickEvent.OpenUrl) {
                Util.getPlatform().openUri(click.uri)
            }
            /*?} else {*/
            /*if (click.action == ClickEvent.Action.OPEN_URL) {
                Util.getPlatform().openUri(URI(click.value))
            }
            *//*?}*/
        } else {
            return handleComponentClicked(style)
        }

        return false
    }

    protected fun getStyle(mouseX: Int, mouseY: Int): Style? {
        val y = mouseY - 90
        val line = y / font.lineHeight

        if (y < 0 || line !in wrappedText.indices) return null
        val text = wrappedText[line]

        val x = mouseX - (width / 2 - font.width(text) / 2)
        return font.splitter.componentStyleAtWidth(text, x)

    }
}
