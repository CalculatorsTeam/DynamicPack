package com.adamcalculator.dynamicpack.client;

import com.adamcalculator.dynamicpack.DynamicPackMod;
import com.adamcalculator.dynamicpack.pack.DynamicResourcePack;
import com.adamcalculator.dynamicpack.pack.OverrideType;
import com.adamcalculator.dynamicpack.pack.dynamicrepo.BaseContent;
import com.adamcalculator.dynamicpack.pack.dynamicrepo.BaseEnum;
import com.adamcalculator.dynamicpack.pack.dynamicrepo.DynamicRepoPreferences;
import com.adamcalculator.dynamicpack.pack.dynamicrepo.DynamicRepoRemote;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.function.Consumer;

public class ContentsScreen extends Screen {
    private final Screen parent;
    private final DynamicResourcePack pack;
    private final DynamicRepoPreferences preferences;
    private final Consumer<DynamicResourcePack> onPackReSync = pack -> Compat.runAtUI(this::onClose);
    private ContentsList contentsList;
    private boolean syncOnExit = false;
    private Button doneButton;
    private Button resetButton;

    protected final HashMap<String, BaseContent> contentIdsAssociation = new HashMap<>();
    protected final LinkedHashMap<BaseContent, OverrideType> preChangeStates = new LinkedHashMap<>();
    protected final LinkedHashSet<BaseEnum> enums = new LinkedHashSet<>();

    protected ContentsScreen(Screen parent, DynamicResourcePack pack) {
        super(Component.translatable("dynamicpack.screen.pack_contents.title"));
        this.parent = parent;
        this.pack = pack;
        this.minecraft = Minecraft.getInstance();
        this.pack.addDestroyListener(onPackReSync);
        this.preferences = ((DynamicRepoRemote) pack.getRemote()).getPreferences();

        for (BaseContent knownContent : preferences.getKnownContents()) {
            contentIdsAssociation.put(knownContent.getId(), knownContent);
            preChangeStates.put(knownContent, knownContent.getOverride());
        }

        enums.addAll(Arrays.asList(preferences.getKnownEnums()));
    }

    public boolean isChanges() {
        boolean t = false;

        for (BaseContent knownContent : preChangeStates.keySet()) {
            if (preChangeStates.get(knownContent) != knownContent.getOverride()) {
                t = true;
                break;
            }
        }
        return t;
    }

    public void reset() {
        for (BaseContent knownContent : preChangeStates.keySet()) {
            OverrideType overrideType = preChangeStates.get(knownContent);
            knownContent.setOverrideType(overrideType, getBaseContents());
        }

        contentsList.refreshAll();
        onAfterChange();
    }


    public void onAfterChange() {
        this.syncOnExit = isChanges();
        updateDoneButton();
    }

    @Override
    public void onClose() {
        applyChanges();

        Minecraft.getInstance().setScreen(this.parent);
        pack.removeDestroyListener(onPackReSync);
        if (syncOnExit) {
            DynamicPackMod.getInstance().startManuallySync(pack);
        }
    }

    private void applyChanges() {
        for (BaseContent baseContent : preChangeStates.keySet()) {
            preferences.setContentOverride(baseContent, baseContent.getOverride());
        }
        pack.saveClientFile();
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return !syncOnExit;
    }

    @Override
    public void render(@NotNull GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        contentsList.render(context, mouseX, mouseY, delta);
        Compat.drawCenteredString(context, this.font, this.title, this.width / 2, 8, 0xFFFFFFFF);
    }

    @Override
    protected void init() {
        super.init();
        this.addWidget(this.contentsList = new ContentsList(this, this.minecraft));
        this.addRenderableWidget(doneButton = Compat.createButton(CommonComponents.GUI_DONE, this::onClose, 150, 20, this.width / 2 + 4, this.height - 26));
        this.addRenderableWidget(resetButton = Compat.createButton(Component.translatable("controls.reset"), this::reset, 150, 20, this.width / 2 + 4-160+2, this.height - 26));
        updateDoneButton();
    }

    private void updateDoneButton() {
        if (syncOnExit) {
            doneButton.setMessage(Component.translatable("dynamicpack.screen.pack_contents.apply").withStyle(ChatFormatting.BOLD, ChatFormatting.GOLD));
            doneButton.setTooltip(Tooltip.create(Component.translatable("dynamicpack.screen.pack_contents.apply.tooltip")));
        } else {
            doneButton.setMessage(CommonComponents.GUI_DONE);
            doneButton.setTooltip(null);
        }
        resetButton.visible = syncOnExit;
    }

    public BaseContent[] getBaseContents() {
        return preChangeStates.keySet().toArray(new BaseContent[0]);
    }

    public BaseEnum[] getBaseEnum() {
        return enums.toArray(new BaseEnum[0]);
    }

    public BaseContent getById(String id) {
        return contentIdsAssociation.getOrDefault(id, null);
    }
}
