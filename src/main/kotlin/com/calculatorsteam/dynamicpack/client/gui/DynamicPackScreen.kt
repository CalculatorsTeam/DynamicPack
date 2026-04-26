package com.calculatorsteam.dynamicpack.client.gui

import com.calculatorsteam.dynamicpack.client.config.Config
import com.calculatorsteam.dynamicpack.DynamicPackMod
import com.calculatorsteam.dynamicpack.Constants
import com.calculatorsteam.dynamicpack.pack.DynamicResourcePack
import com.calculatorsteam.dynamicpack.pack.dynamicrepo.DynamicRepoRemote
import com.calculatorsteam.dynamicpack.platform.GuiGraphicsExtractor
import com.calculatorsteam.dynamicpack.platform.VersionFunctions
import com.calculatorsteam.dynamicpack.sync.SyncingTask
import com.calculatorsteam.dynamicpack.util.log.NetworkStat
import com.calculatorsteam.dynamicpack.util.exception.TranslatableException
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.Tooltip
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import java.awt.Color
import java.text.DateFormat
import java.util.Date
import kotlin.math.sin

class DynamicPackScreen(
    private val parent: Screen?,
    private var pack: DynamicResourcePack
) : Screen(Component.literal(pack.name).withStyle(ChatFormatting.BOLD)) {

    private val destroyListener: (DynamicResourcePack) -> Unit = { setPack(it) }
    private lateinit var syncButton: Button
    private lateinit var contentsButton: Button
    private lateinit var syncButtonThis: Button
    private lateinit var syncButtonAll: Button

    init {
        setPack(pack)
    }

    private fun setPack(pack: DynamicResourcePack) {
        this.pack.removeDestroyListener(destroyListener)
        this.pack = pack
        pack.addDestroyListener(destroyListener)
    }

    /*? if >=26.1 {*/
    override fun extractRenderState(context: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, delta: Float) {
    /*?} else {*/
    /*override fun render(context: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, delta: Float) {
    *//*?}*/
        /*? if >=26.1 {*/
        super.extractRenderState(context, mouseX, mouseY, delta)
        /*?} else if >=1.21 {*/
        /*super.render(context, mouseX, mouseY, delta)
        *//*?} else {*/
        /*VersionFunctions.renderBackground(this, context, mouseX, mouseY, delta);
        *//*?}*/

        syncButton.active = !SyncingTask.isSyncing()
        contentsButton.active = !SyncingTask.isSyncing()

        if (SyncingTask.isSyncing()) {
            hideSyncSpecButtons()
        }

        val h = 20
        VersionFunctions.text(context, this.font, this.title, 20, 8, -1)
        VersionFunctions.drawWrappedString(
            context,
            Component.translatable("dynamicpack.screen.pack.description").getString(999),
            20, 20 + h,
            width - 125, 2,
            -11141291
        )
        VersionFunctions.text(
            context, this.font,
            Component.translatable("dynamicpack.screen.pack.remote_type", pack.getRemoteType()),
            20, 40 + h, -1
        )

        if (SyncingTask.isSyncing()) {
            VersionFunctions.drawWrappedString(
                context,
                SyncingTask.getLogs(),
                20, 78 + 30 + h,
                500, 99,
                -3355444
            )

            val asciiPercentage = StringBuilder()
            var percentage = 0
            try {
                val builder = SyncingTask.currentRootSyncBuilder
                val down = builder?.downloadedSize ?: 0
                val total = builder?.updateSize ?: 0
                percentage = if (total > 0) ((down.toFloat() / total) * 100).toInt() else 0

                repeat(25) { i ->
                    val filled = percentage / 4
                    asciiPercentage.append(if (i >= filled) "_" else "#")
                }
            } catch (_: Exception) {
            }
            VersionFunctions.drawWrappedString(
                context,
                Component.translatable(
                    "dynamicpack.screen.pack.updateStat",
                    Constants.speedToString(NetworkStat.getSpeed()),
                    Constants.secondsToString(SyncingTask.eta),
                    percentage,
                    "[$asciiPercentage]"
                ).getString(512),
                20, 52 + h,
                width, 3,
                Color.getHSBColor(
                    sin(System.currentTimeMillis() / 1850.0).toFloat(),
                    0.6f, 0.6f
                ).rgb
            )
        } else {
            val latestUpdated = pack.getLatestUpdated()
            if (latestUpdated > 0) {
                val date = Date(latestUpdated * 1000)
                val string = DateFormat.getDateTimeInstance().format(date)
                VersionFunctions.text(
                    context,
                    this.font,
                    Component.translatable("dynamicpack.screen.pack.latestUpdated", string),
                    20, 52 + h, -1
                )
            }

            val exception = pack.getLatestException()
            if (exception != null) {
                VersionFunctions.drawWrappedString(
                    context,
                    Component.translatable(
                        "dynamicpack.screen.pack.latestException",
                        TranslatableException.getComponentFromException(exception)
                    ).getString(512),
                    20, 82 + h,
                    width - 40, 4,
                    -56798
                )
            }
        }

        //? if < 1.21
        //super.render(context, mouseX, mouseY, delta)
    }

    private fun hideSyncSpecButtons() {
        syncButtonThis.visible = false
        syncButtonAll.visible = false
    }

    override fun init() {
        addRenderableWidget(
            VersionFunctions.createButton<Button>(
                Component.translatable("dynamicpack.screen.pack.manually_sync"),
                {
                    syncButtonThis.visible = !syncButtonThis.visible
                    syncButtonAll.visible = !syncButtonAll.visible
                },
                100, 20, width - 120, 10
            ).also { syncButton = it }
        )

        addRenderableWidget(
            VersionFunctions.createButton<Button>(
                Component.translatable("dynamicpack.screen.pack.manually_sync.this"),
                {
                    DynamicPackMod.instance.startManuallySync(pack)
                    hideSyncSpecButtons()
                },
                48, 20, width - 120, 35
            ).also { syncButtonThis = it }
        )

        addRenderableWidget(
            VersionFunctions.createButton<Button>(
                Component.translatable("dynamicpack.screen.pack.manually_sync.all"),
                {
                    DynamicPackMod.instance.startManuallySync()
                    hideSyncSpecButtons()
                },
                48, 20, width - 66, 35
            ).also { syncButtonAll = it }
        )

        if (!DynamicPackMod.isResourcePackActive(pack) && Config.getInstance().isUpdateOnlyEnabledPacks()) {
            VersionFunctions.setTooltip(syncButtonAll, Tooltip.create(
                Component.translatable(
                    "dynamicpack.screen.pack.manually_sync.all.warningNotInclude"
                ).withStyle(ChatFormatting.RED)
            ))
        }

        hideSyncSpecButtons()

        addRenderableWidget(
            VersionFunctions.createButton<Button>(
                CommonComponents.GUI_DONE,
                { this.onClose() },
                150, 20, this.width / 2 + 4, this.height - 26
            )
        )

        addRenderableWidget(
            VersionFunctions.createButton<Button>(
                Component.translatable("dynamicpack.screen.pack.dynamic.contents"),
                { Minecraft.getInstance().setScreen(ContentsScreen(this, pack)) },
                150, 20, this.width / 2 - 154, this.height - 26
            ).also { contentsButton = it }
        )
        contentsButton.visible = pack.getRemote() is DynamicRepoRemote
    }

    override fun onClose() {
        Minecraft.getInstance().setScreen(parent)
        pack.removeDestroyListener(destroyListener)
    }
}