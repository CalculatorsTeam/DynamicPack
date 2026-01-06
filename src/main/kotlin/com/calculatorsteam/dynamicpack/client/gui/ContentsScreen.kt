package com.calculatorsteam.dynamicpack.client.gui

import com.calculatorsteam.dynamicpack.DynamicPackMod
import com.calculatorsteam.dynamicpack.pack.DynamicResourcePack
import com.calculatorsteam.dynamicpack.util.enums.OverrideType
import com.calculatorsteam.dynamicpack.pack.dynamicrepo.BaseContent
import com.calculatorsteam.dynamicpack.pack.dynamicrepo.BaseEnum
import com.calculatorsteam.dynamicpack.pack.dynamicrepo.DynamicRepoPreferences
import com.calculatorsteam.dynamicpack.pack.dynamicrepo.DynamicRepoRemote
import com.calculatorsteam.dynamicpack.platform.VersionFunctions
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.Tooltip
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component

class ContentsScreen(
    private val parent: Screen,
    private val pack: DynamicResourcePack
) : Screen(Component.translatable("dynamicpack.screen.pack_contents.title")) {

    private val preferences: DynamicRepoPreferences
    private val onPackReSync: (DynamicResourcePack) -> Unit = { VersionFunctions.runAtUI { this.onClose() } }
    private lateinit var contentsList: ContentsList
    private var syncOnExit = false
    private lateinit var doneButton: Button
    private lateinit var resetButton: Button

    protected val contentIdsAssociation = hashMapOf<String, BaseContent>()
    protected val preChangeStates = linkedMapOf<BaseContent, OverrideType>()
    protected val enums = linkedSetOf<BaseEnum>()

    init {
        this.minecraft = Minecraft.getInstance()
        this.pack.addDestroyListener(onPackReSync)
        this.preferences = (pack.getRemote() as DynamicRepoRemote).preferences

        preferences.getKnownContents().forEach { knownContent ->
            contentIdsAssociation[knownContent.id] = knownContent
            preChangeStates[knownContent] = knownContent.overrideType
        }
        enums.addAll(preferences.getKnownEnums())
    }

    fun isChanges(): Boolean =
        preChangeStates.any { (content, override) -> override != content.overrideType }

    fun reset() {
        preChangeStates.forEach { (content, overrideType) ->
            content.setOverrideType(overrideType, baseContents)
        }
        contentsList.refreshAll()
        onAfterChange()
    }

    fun onAfterChange() {
        this.syncOnExit = isChanges()
        updateDoneButton()
    }

    override fun onClose() {
        applyChanges()
        Minecraft.getInstance().setScreen(this.parent)
        pack.removeDestroyListener(onPackReSync)
        if (syncOnExit) {
            DynamicPackMod.instance.startManuallySync(pack)
        }
    }

    private fun applyChanges() {
        preChangeStates.keys.forEach { baseContent ->
            preferences.setContentOverride(baseContent, baseContent.overrideType)
        }
        pack.saveClientFile()
    }

    override fun shouldCloseOnEsc(): Boolean = !syncOnExit

    override fun render(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        /*? if >=1.21 {*/
        super.render(context, mouseX, mouseY, delta)
        /*?} else {*/
        /*VersionFunctions.renderBackground(this, context, mouseX, mouseY, delta);
        *//*?}*/
        contentsList.render(context, mouseX, mouseY, delta)
        VersionFunctions.drawCenteredString(context, this.font, this.title, this.width / 2, 8, -1)
        //? if < 1.21
        //super.render(context, mouseX, mouseY, delta)
    }

    override fun init() {
        super.init()
        this.contentsList = ContentsList(this, this.minecraft!!)
        this.addWidget(contentsList)

        this.addRenderableWidget(
            VersionFunctions.createButton<Button>(
                CommonComponents.GUI_DONE,
                { this.onClose() },
                150, 20,
                this.width / 2 + 4, this.height - 26
            ).also { doneButton = it }
        )
        this.addRenderableWidget(
            VersionFunctions.createButton<Button>(
                Component.translatable("controls.reset"),
                { this.reset() },
                150, 20,
                this.width / 2 - 154, this.height - 26
            ).also { resetButton = it }
        )
        updateDoneButton()
    }

    private fun updateDoneButton() {
        if (syncOnExit) {
            doneButton.message = Component.translatable("dynamicpack.screen.pack_contents.apply")
                .withStyle(ChatFormatting.BOLD, ChatFormatting.GOLD)
            VersionFunctions.setTooltip(doneButton, Tooltip.create(
                Component.translatable("dynamicpack.screen.pack_contents.apply.tooltip")
            ))
        } else {
            doneButton.message = CommonComponents.GUI_DONE
            VersionFunctions.setTooltip(doneButton, null)
        }
        resetButton.visible = syncOnExit
    }

    val baseContents: Array<BaseContent>
        get() = preChangeStates.keys.toTypedArray()

    val baseEnum: Array<BaseEnum>
        get() = enums.toTypedArray()

    fun getById(id: String): BaseContent? =
        contentIdsAssociation[id]
}