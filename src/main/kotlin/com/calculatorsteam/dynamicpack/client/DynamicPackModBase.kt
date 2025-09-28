package com.calculatorsteam.dynamicpack.client

import com.calculatorsteam.dynamicpack.DynamicPackMod
import com.calculatorsteam.dynamicpack.Constants
import com.calculatorsteam.dynamicpack.pack.DynamicResourcePack
import com.calculatorsteam.dynamicpack.platform.VersionFunctions
import com.calculatorsteam.dynamicpack.util.status.StatusChecker
import com.calculatorsteam.dynamicpack.sync.SyncThread
import com.calculatorsteam.dynamicpack.util.log.Out
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.internal.LazilyParsedNumber
import net.minecraft.ChatFormatting
import net.minecraft.SharedConstants
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.toasts.SystemToast
import net.minecraft.client.player.LocalPlayer
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.util.GsonHelper
import java.net.URI

/**
 * Base impl for DynamicPack mod (minecraft logic)
 * <pre>
 * == inheritance tree ==
 * DynamicPackMod - mod-related logic
 * | DynamicPackModBase - minecraft-related logic
 * . | fabric impl...
 * . | forge impl...
 * . | ...
 * </pre>
 */
abstract class DynamicPackModBase : DynamicPackMod() {

    private var toast: SystemToast? = null
    private var toastUpdated: Long = 0

    fun setToastContent(title: Component, text: Component) {
        if (!isMinecraftInitialized()) return

        val now = System.currentTimeMillis()
        if (toast == null || (now - toastUpdated > 5000)) {
            val toastManager = VersionFunctions.getToastManager()
            toast = SystemToast(
                VersionFunctions.getSystemToastId(),
                title,
                text
            ).also { toastManager.addToast(it) }
        } else {
            toast?.reset(title, text)
        }
        toastUpdated = now
    }

    private fun createDownloadComponent(): Component =
        Component.translatable("dynamicpack.status_checker.download")
            .withStyle(
                Style.EMPTY
                    .withHoverEvent(
                        VersionFunctions.getHoverEventShowText(
                            Component.translatable(
                                "dynamicpack.status_checker.download.hover",
                                Component.literal(Constants.MODRINTH_URL)
                                    .withStyle(ChatFormatting.UNDERLINE, ChatFormatting.AQUA)
                            )
                        )
                    )
                    .withClickEvent(
                        VersionFunctions.getClickEventOpenUrl(Constants.MODRINTH_URL)
                    )
            )
            .withStyle(ChatFormatting.YELLOW, ChatFormatting.UNDERLINE)

    fun onWorldJoinForUpdateChecks(player: LocalPlayer?) {
        if (Constants.isDebugMessageOnWorldJoin()) {
            VersionFunctions.displayClientMessage(
                player,
                Component.literal("Debug message on world join")
                    .withStyle(ChatFormatting.GREEN)
            )
        }
        when {
            player == null -> Out.warn("player == null on world join")
            !StatusChecker.isSafe() -> {
                VersionFunctions.displayClientMessage(
                    player,
                    Component.translatable(
                        "dynamicpack.status_checker.not_safe",
                        createDownloadComponent()
                    )
                )
                setToastContent(
                    Component.translatable("dynamicpack.status_checker.not_safe.toast.title"),
                    Component.translatable("dynamicpack.status_checker.not_safe.toast.description")
                )
            }
            !StatusChecker.isFormatActual() -> VersionFunctions.displayClientMessage(
                player,
                Component.translatable(
                    "dynamicpack.status_checker.format_not_actual",
                    createDownloadComponent()
                )
            )

            StatusChecker.isModUpdateAvailable() ->
                Out.println("DynamicPack mod update available: ${Constants.MODRINTH_URL}")

            !StatusChecker.isChecked() ->
                Out.warn("StatusChecker isChecked = false :(")

            else -> Out.println("Mod in actual state in current time!")
        }
    }

    override fun startManuallySync() {
        SyncThread("SyncThread-${manuallySyncThreadCounter++}").start()
    }

    override fun startManuallySync(pack: DynamicResourcePack) {
        SyncThread("SyncThread-${manuallySyncThreadCounter++}", pack).start()
    }

    override fun getCurrentGameVersion(): String {
        SharedConstants.tryDetectVersion()
        return VersionFunctions.versionName(SharedConstants.getCurrentVersion())
    }

    override fun checkResourcePackMetaValid(s: String): Boolean {
        val pack: JsonObject = GsonHelper.parse(s).getAsJsonObject("pack")
        if (pack["pack_format"].asNumber is LazilyParsedNumber) {
            (pack["pack_format"].asNumber as LazilyParsedNumber).toInt()
        }
        val description: JsonElement = pack["description"]
        if (description.isJsonNull) {
            throw NullPointerException("description is null in pack.mcmeta")
        }
        return true
    }

    override fun needResourcesReload() {
        val client = Minecraft.getInstance()
        if (client != null && client.level == null) {
            client.execute { client.reloadResourcePacks() }
        } else {
            setToastContent(
                Component.translatable("dynamicpack.toast.needReload"),
                Component.translatable("dynamicpack.toast.needReload.description")
            )
        }
    }
}