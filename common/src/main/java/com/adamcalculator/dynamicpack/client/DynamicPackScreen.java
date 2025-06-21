package com.adamcalculator.dynamicpack.client;

import com.adamcalculator.dynamicpack.Config;
import com.adamcalculator.dynamicpack.DynamicPackMod;
import com.adamcalculator.dynamicpack.SharedConstrains;
import com.adamcalculator.dynamicpack.pack.DynamicResourcePack;
import com.adamcalculator.dynamicpack.pack.dynamicrepo.DynamicRepoRemote;
import com.adamcalculator.dynamicpack.sync.SyncingTask;
import com.adamcalculator.dynamicpack.util.NetworkStat;
import com.adamcalculator.dynamicpack.util.TranslatableException;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.text.DateFormat;
import java.util.Date;
import java.util.function.Consumer;

public class DynamicPackScreen extends Screen {
    private final Screen parent;
    private DynamicResourcePack pack;
    private Button syncButton;
    private final Consumer<DynamicResourcePack> destroyListener = this::setPack;
    private Button contentsButton;
    private Button syncButtonThis;
    private Button syncButtonAll;

    public DynamicPackScreen(Screen parent, DynamicResourcePack pack) {
        super(Component.literal(pack.getName()).withStyle(ChatFormatting.BOLD));
        this.pack = pack;
        this.minecraft = Minecraft.getInstance();
        this.parent = parent;
        setPack(pack);
    }

    private void setPack(DynamicResourcePack pack) {
        if (this.pack != null) {
            this.pack.removeDestroyListener(destroyListener);
        }
        this.pack = pack;
        pack.addDestroyListener(destroyListener);
    }

    @Override
    public void render(@NotNull GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        syncButton.active = !SyncingTask.isSyncing();
        contentsButton.active = !SyncingTask.isSyncing();

        if (SyncingTask.isSyncing()) {
            hideSyncSpecButtons();
        }

        int h = 20;
        Compat.drawString(context, this.font, this.title, 20, 8, 0xFFFFFFFF);
        Compat.drawWrappedString(context, Component.translatable("dynamicpack.screen.pack.description").getString(999), 20, 20 + h, width - 125, 2, 0xFF55FF55);
        Compat.drawString(context, this.font, Component.translatable("dynamicpack.screen.pack.remote_type", pack.getRemoteType()), 20, 40 + h, 0xFFFFFFFF);

        if (SyncingTask.isSyncing()) {
            Compat.drawWrappedString(context, SyncingTask.getLogs(), 20, 78+30 + h, 500, 99, 0xFFCCCCCC);

            StringBuilder asciiPercentage = new StringBuilder();
            int percentage = 0;

            try {
                percentage = (int) ((float)SyncingTask.currentRootSyncBuilder.getDownloadedSize() / (float)SyncingTask.currentRootSyncBuilder.getUpdateSize() * 100f);
                for (int i = 0; i < 25; i++) {
                    int i1 = percentage / 4;
                    if (i >= i1) {
                        asciiPercentage.append("_");
                    } else {
                        asciiPercentage.append("#");
                    }
                }
            } catch (Exception ignored) {}
            Compat.drawWrappedString(context, Component.translatable("dynamicpack.screen.pack.updateStat", SharedConstrains.speedToString(NetworkStat.getSpeed()), SharedConstrains.secondsToString(SyncingTask.eta), percentage, "["+asciiPercentage+"]").getString(512), 20, 52 + h, width, 3, Color.getHSBColor((float)Math.sin(System.currentTimeMillis()/1850d), 0.6f, 0.6f).getRGB());

        } else {
            long latestUpdated;
            if ((latestUpdated = pack.getLatestUpdated()) > 0) {
                Date date = new Date(latestUpdated * 1000);
                String string = DateFormat.getDateTimeInstance().format(date);
                Compat.drawString(context, this.font, Component.translatable("dynamicpack.screen.pack.latestUpdated", string), 20, 52 + h, 0xFFFFFFFF);
            }

            h += 4;
            Exception exception = pack.getLatestException();
            if (exception != null) {
                Compat.drawWrappedString(context, Component.translatable("dynamicpack.screen.pack.latestException", TranslatableException.getComponentFromException(exception)).getString(512), 20, 78 + h, width - 40, 4, 0xFFFF2222);
            }
        }
    }

    private void hideSyncSpecButtons() {
        syncButtonThis.visible = false;
        syncButtonAll.visible = false;
    }

    @Override
    protected void init() {
        addRenderableWidget(syncButton = Compat.createButton(
                Component.translatable("dynamicpack.screen.pack.manually_sync"),
                () -> {
                    syncButtonThis.visible = !syncButtonThis.visible;
                    syncButtonAll.visible = !syncButtonAll.visible;
                },
                100, 20, width - 120, 10
        ));

        addRenderableWidget(syncButtonThis = Compat.createButton(
                Component.translatable("dynamicpack.screen.pack.manually_sync.this"),
                () -> {
                    DynamicPackMod.getInstance().startManuallySync(pack);
                    hideSyncSpecButtons();
                },
                48, 20, width - 120, 35
        ));

        addRenderableWidget(syncButtonAll = Compat.createButton(
                Component.translatable("dynamicpack.screen.pack.manually_sync.all"),
                () -> {
                    DynamicPackMod.getInstance().startManuallySync();
                    hideSyncSpecButtons();
                },
                48, 20, width - 120 + 54, 35
        ));
        if (!DynamicPackMod.isResourcePackActive(pack) && Config.getInstance().isUpdateOnlyEnabledPacks()) {
            syncButtonAll.setTooltip(Tooltip.create(Component.translatable("dynamicpack.screen.pack.manually_sync.all.warningNotInclude").withStyle(ChatFormatting.RED)));
        }

        hideSyncSpecButtons();

        addRenderableWidget(Compat.createButton(CommonComponents.GUI_DONE, this::onClose, 150, 20, this.width / 2 + 4, this.height - 20 - 6));
        addRenderableWidget(contentsButton = Compat.createButton(Component.translatable("dynamicpack.screen.pack.dynamic.contents"), () -> {
            Minecraft.getInstance().setScreen(new ContentsScreen(this, pack));
        }, 150, 20, this.width / 2 + 4-160+2, this.height - 20 - 6));
        contentsButton.visible = pack.getRemote() instanceof DynamicRepoRemote;
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
        pack.removeDestroyListener(destroyListener);
    }
}
