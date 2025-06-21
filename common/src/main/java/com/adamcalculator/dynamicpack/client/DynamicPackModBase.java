package com.adamcalculator.dynamicpack.client;

import com.adamcalculator.dynamicpack.DynamicPackMod;
import com.adamcalculator.dynamicpack.SharedConstrains;
import com.adamcalculator.dynamicpack.pack.DynamicResourcePack;
import com.adamcalculator.dynamicpack.status.StatusChecker;
import com.adamcalculator.dynamicpack.sync.SyncThread;
import com.adamcalculator.dynamicpack.util.Out;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.internal.LazilyParsedNumber;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.GsonHelper;

import java.net.URI;


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
public abstract class DynamicPackModBase extends DynamicPackMod {
    private SystemToast toast = null;
    private long toastUpdated = 0;

    public void setToastContent(Component title, Component text) {
        if (!isMinecraftInitialized()) {
            return;
        }

        if (toast == null || (System.currentTimeMillis() - toastUpdated > 1000*5)) {
            ToastManager toastManager = Minecraft.getInstance().getToastManager();
            toastManager.addToast(toast = new SystemToast(/*1.20.4 port*/new SystemToast.SystemToastId(5000), title, text));
        } else {
            toast.reset(title, text);
        }
        toastUpdated = System.currentTimeMillis();
    }

    private Component createDownloadComponent() {
        return Component.translatable("dynamicpack.status_checker.download")
                .withStyle(Style.EMPTY
                        .withHoverEvent(new HoverEvent.ShowText(Component.translatable("dynamicpack.status_checker.download.hover",
                                Component.literal(SharedConstrains.MODRINTH_URL).withStyle(ChatFormatting.UNDERLINE, ChatFormatting.AQUA))))
                        .withClickEvent(new ClickEvent.OpenUrl(URI.create(SharedConstrains.MODRINTH_URL)))
                )
                .withStyle(ChatFormatting.YELLOW, ChatFormatting.UNDERLINE);
    }

    public void onWorldJoinForUpdateChecks(LocalPlayer player) {
        if (SharedConstrains.isDebugMessageOnWorldJoin()) {
            player.displayClientMessage(Component.literal("Debug message on world join").withStyle(ChatFormatting.GREEN), true);
        }

        if (player == null) {
            Out.warn("player == null on world join");

        } else if (!StatusChecker.isSafe()) {
            player.displayClientMessage(Component.translatable("dynamicpack.status_checker.not_safe", createDownloadComponent()), true);
            setToastContent(Component.translatable("dynamicpack.status_checker.not_safe.toast.title"),
                    Component.translatable("dynamicpack.status_checker.not_safe.toast.description"));

        } else if (!StatusChecker.isFormatActual()) {
            player.displayClientMessage(Component.translatable("dynamicpack.status_checker.format_not_actual", createDownloadComponent()), true);

        } else if (StatusChecker.isModUpdateAvailable()) {
            Out.println("DynamicPack mod update available: " + SharedConstrains.MODRINTH_URL);

        } else if (!StatusChecker.isChecked()) {
            Out.warn("StatusChecker isChecked = false :(");

        } else {
            Out.println("Mod in actual state in current time!");
        }
    }

    @Override
    public void startManuallySync() {
        SyncThread syncThread = new SyncThread("SyncThread-"+(DynamicPackMod.manuallySyncThreadCounter++));
        syncThread.start();
    }

    @Override
    public void startManuallySync(DynamicResourcePack pack) {
        SyncThread syncThread = new SyncThread("SyncThread-"+(DynamicPackMod.manuallySyncThreadCounter++), pack);
        syncThread.start();
    }

    @Override
    public String getCurrentGameVersion() {
        SharedConstants.tryDetectVersion();
        return SharedConstants.getCurrentVersion().getName();
    }

    @Override
    public boolean checkResourcePackMetaValid(String s) {
        // 1.20.4 port
        JsonObject pack = GsonHelper.parse(s).getAsJsonObject("pack");
        if (pack.get("pack_format").getAsNumber() instanceof LazilyParsedNumber lazilyParsedNumber) {
            lazilyParsedNumber.intValue();
        }
        JsonElement description = pack.get("description");
        if (description.isJsonNull()) {
            throw new NullPointerException("description is null in pack.mcmeta");
        }
        //MetadataSectionType.fromCodec("not used in this case string", PackMetadataSection.CODEC).fromJson(GsonHelper.parse(s).getAsJsonObject("pack"));
        return true;
    }

    @Override
    public void needResourcesReload() {
        Minecraft client = Minecraft.getInstance();
        if (client != null && client.level == null) {
            client.execute(client::reloadResourcePacks);

        } else {
            setToastContent(Component.translatable("dynamicpack.toast.needReload"),
                    Component.translatable("dynamicpack.toast.needReload.description"));
        }
    }
}
