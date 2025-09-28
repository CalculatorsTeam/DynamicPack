package com.calculatorsteam.dynamicpack.client.gui.widget

import com.calculatorsteam.dynamicpack.DynamicPackMod
import com.calculatorsteam.dynamicpack.client.gui.DynamicPackScreen
import com.calculatorsteam.dynamicpack.pack.DynamicResourcePack
import com.calculatorsteam.dynamicpack.platform.VersionFunctions
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.packs.PackSelectionModel
import net.minecraft.resources.ResourceLocation
import kotlin.math.cos
import kotlin.math.sin

/**
 * Widget for displaying the status of DynamicPack in the resource pack selection menu.
 */
class DynamicPackResourcePackEntryWidget : ResourcePackEntryWidget {
    override fun isVisible(pack: PackSelectionModel.Entry, selectable: Boolean): Boolean =
        getDynamicPackFromArgs(pack) != null

    override fun render(
        pack: PackSelectionModel.Entry,
        context: GuiGraphics,
        x: Int,
        y: Int,
        hovered: Boolean,
        tickDelta: Float
    ) {
        getDynamicPackFromArgs(pack)?.let { drawTexture(context, it, x, y, hovered) }
    }

    override fun getWidth(pack: PackSelectionModel.Entry): Int = 16
    override fun getHeight(pack: PackSelectionModel.Entry, rowHeight: Int): Int = 16
    override fun getY(pack: PackSelectionModel.Entry, rowHeight: Int): Int = 16
    override fun getXMargin(pack: PackSelectionModel.Entry): Int = 2

    override fun onClick(pack: PackSelectionModel.Entry) {
        getDynamicPackFromArgs(pack)?.let { openPackScreen(it) }
    }

    private fun getDynamicPackFromArgs(entry: PackSelectionModel.Entry): DynamicResourcePack? =
        DynamicPackMod.getDynamicPackByMinecraftName(entry.id)

    companion object {
        private val BUTTON_TEXTURE = ResourceLocation.tryBuild("dynamicpack", "select_button.png")
        private val BUTTON_WARNING_TEXTURE = ResourceLocation.tryBuild("dynamicpack", "select_button_warning.png")
        private val BUTTON_SYNCING_TEXTURE = ResourceLocation.tryBuild("dynamicpack", "select_button_syncing.png")

        fun drawTexture(context: GuiGraphics, pack: DynamicResourcePack, x: Int, y: Int, hovered: Boolean) {
            val latestException = pack.getLatestException()
            when {
                pack.isSyncing -> {
                    VersionFunctions.drawTexture(context, BUTTON_TEXTURE, x, y, 0f, if (hovered) 16f else 0f,
                        16, 16, 16, 32)

                    val alpha = System.currentTimeMillis() / 200.0
                    val xShift = (sin(alpha) * 6.9).toInt()
                    val yShift = (cos(alpha) * 6.9).toInt()

                    VersionFunctions.drawTexture(context, BUTTON_SYNCING_TEXTURE,
                        x + xShift + 6, y + yShift + 6,
                        0f, if (hovered) 16f else 0f,
                        4, 4, 16, 32)
                }
                latestException != null -> {
                    VersionFunctions.drawTexture(context, BUTTON_WARNING_TEXTURE, x, y, 0f,
                        if (hovered) 16f else 0f, 16, 16, 16, 32)
                }
                else -> {
                    VersionFunctions.drawTexture(context, BUTTON_TEXTURE, x, y, 0f,
                        if (hovered) 16f else 0f, 16, 16, 16, 32)
                }
            }
        }

        fun openPackScreen(pack: DynamicResourcePack) {
            val mc = Minecraft.getInstance()
            mc.setScreen(DynamicPackScreen(mc.screen, pack))
        }
    }
}