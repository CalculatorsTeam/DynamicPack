package com.calculatorsteam.dynamicpack.client.gui.widget

import com.calculatorsteam.dynamicpack.platform.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.packs.PackSelectionModel

/**
 * Represents an additional widget inserted on the right-hand side of a resource pack entry
 * (in the resource pack or data pack screen).
 */
interface ResourcePackEntryWidget {
    fun isVisible(pack: PackSelectionModel.Entry, selectable: Boolean): Boolean = true
    fun getWidth(pack: PackSelectionModel.Entry): Int
    fun getHeight(pack: PackSelectionModel.Entry, rowHeight: Int): Int
    fun getY(pack: PackSelectionModel.Entry, rowHeight: Int): Int =
        (rowHeight - getHeight(pack, rowHeight)) / 2
    fun getXMargin(pack: PackSelectionModel.Entry): Int = 7
    fun extractRenderState(
        pack: PackSelectionModel.Entry,
        context: GuiGraphicsExtractor,
        x: Int,
        y: Int,
        hovered: Boolean,
        tickDelta: Float
    )
    fun onClick(pack: PackSelectionModel.Entry)
}