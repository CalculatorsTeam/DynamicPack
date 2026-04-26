package com.calculatorsteam.dynamicpack.client.gui

import com.calculatorsteam.dynamicpack.client.config.Config
import com.calculatorsteam.dynamicpack.pack.dynamicrepo.BaseContent
import com.calculatorsteam.dynamicpack.pack.dynamicrepo.BaseEnum
import com.calculatorsteam.dynamicpack.platform.GuiGraphicsExtractor
import com.calculatorsteam.dynamicpack.platform.VersionFunctions
import com.calculatorsteam.dynamicpack.util.enums.OverrideType
import com.calculatorsteam.dynamicpack.util.log.Out
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.ContainerObjectSelectionList
import net.minecraft.client.gui.components.Tooltip
import net.minecraft.client.gui.components.events.GuiEventListener
import net.minecraft.client.gui.narration.NarratableEntry
import net.minecraft.network.chat.Component

/**
 * List widget for showing contents (BaseContent and BaseEnum entries).
 */
class ContentsList(
    private val parent: ContentsScreen,
    minecraft: Minecraft
) : ContainerObjectSelectionList<ContentsList.ContentEntry>(minecraft, parent.width, parent.height/*? if >=1.20.4 {*/- 52, 20,/*?} else {*//*, 20, parent.height - 32,*//*?}*/40) {

    init {
        // contents
        for (knownContent in parent.baseContents) {
            if (knownContent.hidden && !Config.getInstance().dynamicRepoIsIgnoreHiddenContentFlag()) {
                continue
            }
            addEntry(BaseContentEntry(knownContent))
        }
        // enums
        for (anEnum in parent.baseEnum) {
            addEntry(EnumContentEntry(anEnum))
        }
    }
    /*? if >=1.21.4 {*/
    override fun scrollBarX(): Int = super.scrollBarX() + 15
    /*?} else {*/
    /*override fun getScrollbarPosition(): Int = super.getScrollbarPosition() + 15
    *//*?}*/
    override fun getRowWidth(): Int = super.getRowWidth() + 32

    fun refreshAll() {
        children().forEach { it.refresh() }
    }

    /**
     * Row with BaseContent
     */
    inner class BaseContentEntry(private val content: BaseContent) : ContentEntry() {
        init {
            stateButton = createStateButton()
            stateButton.active = !content.required
            if (!stateButton.active) {
                VersionFunctions.setTooltip(stateButton, Tooltip.create(
                    Component.translatable("dynamicpack.screen.pack_contents.state.tooltip_disabled")
                ))
            }
        }

        private fun createStateButton(): Button =
            Button.builder(
                Component.translatable("dynamicpack.screen.pack_contents.state", currentState())
            ) { clicked() }
                .bounds(0, 0, 140, 20)
                .build()

        private fun clicked() {
            try {
                content.nextOverride(parent.baseContents)
            } catch (e: Exception) {
                Out.error("Error while content.nextOverride() in gui", e)
            }
            parent.onAfterChange()
            refreshAll()
        }

        override fun refresh() {
            stateButton.message =
                Component.translatable("dynamicpack.screen.pack_contents.state", currentState())
        }

        private fun currentState(): Component {
            val key = when (content.overrideType) {
                OverrideType.TRUE -> "dynamicpack.screen.pack_contents.state.true"
                OverrideType.FALSE -> "dynamicpack.screen.pack_contents.state.false"
                OverrideType.NOT_SET ->
                    if (content.defaultState)
                        "dynamicpack.screen.pack_contents.state.default.true"
                    else
                        "dynamicpack.screen.pack_contents.state.default.false"
            }
            return Component.translatable(key)
        }

        /*? if >=26.1 {*/
        override fun extractContent(
            context: GuiGraphicsExtractor,
            mouseX: Int,
            mouseY: Int,
            hovered: Boolean,
            tickDelta: Float
        )
        /*?} else if >=1.21.9 {*/
        /*override fun renderContent(
            context: GuiGraphicsExtractor,
            mouseX: Int,
            mouseY: Int,
            hovered: Boolean,
            tickDelta: Float
        )
        *//*?} else {*/
        /*override fun render(
            context: GuiGraphicsExtractor,
            index: Int,
            y: Int,
            x: Int,
            entryWidth: Int,
            entryHeight: Int,
            mouseX: Int,
            mouseY: Int,
            hovered: Boolean,
            tickDelta: Float
        )
        *//*?}*/
        {
            val txt = content.name ?: content.id
            val text = Component.literal(txt)
            VersionFunctions.text(context, this@ContentsList.minecraft.font, text, x - 50, y + 10, -1, false)
            /*? if >=1.21.9 {*/
            stateButton.x = x + this.width - 138
            /*?} else {*/
            /*stateButton.x = x + entryWidth - 140
            *//*?}*/
            stateButton.y = y
            /*? if >=26.1 {*/
            stateButton.extractRenderState(context, mouseX, mouseY, tickDelta)
            /*?} else {*/
            /*stateButton.render(context, mouseX, mouseY, tickDelta)
            *//*?}*/
        }
    }

    /**
     * Row with BaseEnum
     */
    inner class EnumContentEntry(private val baseEnum: BaseEnum) : ContentEntry() {
        init {
            stateButton = createStateButton()
        }

        private fun createStateButton(): Button =
            Button.builder(currentState()) { clicked() }
                .bounds(0, 0, 140, 20)
                .build()

        private fun clicked() {
            try {
                baseEnum.applyNext(parent.baseContents)
            } catch (e: Exception) {
                Out.error("Error while applyNext (gui)", e)
            }
            parent.onAfterChange()
            refreshAll()
        }

        override fun refresh() {
            stateButton.message = currentState()
        }

        private fun currentState(): Component =
            Component.literal(baseEnum.getCurrentState(parent.baseContents))

        /*? if >=26.1 {*/
        override fun extractContent(
            context: GuiGraphicsExtractor,
            mouseX: Int,
            mouseY: Int,
            hovered: Boolean,
            tickDelta: Float
        )
        /*?} else if >=1.21.9 {*/
        /*override fun renderContent(
            context: GuiGraphicsExtractor,
            mouseX: Int,
            mouseY: Int,
            hovered: Boolean,
            tickDelta: Float
        )
        *//*?} else {*/
        /*override fun render(
            context: GuiGraphicsExtractor,
            index: Int,
            y: Int,
            x: Int,
            entryWidth: Int,
            entryHeight: Int,
            mouseX: Int,
            mouseY: Int,
            hovered: Boolean,
            tickDelta: Float
        )
        *//*?}*/
        {
            val txt = baseEnum.name ?: baseEnum.id
            val text = Component.literal(txt)
            VersionFunctions.text(context, this@ContentsList.minecraft.font, text, x - 50, y + 10, -1, false)
            /*? if >=1.21.9 {*/
            stateButton.x = x + this.width - 138
            /*?} else {*/
            /*stateButton.x = x + entryWidth - 140
            *//*?}*/
            stateButton.y = y
            /*? if >=26.1 {*/
            stateButton.extractRenderState(context, mouseX, mouseY, tickDelta)
            /*?} else {*/
            /*stateButton.render(context, mouseX, mouseY, tickDelta)
            *//*?}*/
        }
    }

    /**
     * Base row type
     */
    abstract inner class ContentEntry : Entry<ContentEntry>() {
        protected lateinit var stateButton: Button
        abstract fun refresh()

        override fun children(): List<GuiEventListener> = listOf(stateButton)
        override fun narratables(): List<NarratableEntry> = listOf(stateButton)
    }
}