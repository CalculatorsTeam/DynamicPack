package com.adamcalculator.dynamicpack.client;

import com.adamcalculator.dynamicpack.Config;
import com.adamcalculator.dynamicpack.pack.dynamicrepo.BaseContent;
import com.adamcalculator.dynamicpack.pack.dynamicrepo.BaseEnum;
import com.adamcalculator.dynamicpack.util.Out;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ContentsList extends ContainerObjectSelectionList<ContentsList.ContentEntry> {
    private final ContentsScreen parent;

    public ContentsList(ContentsScreen parent, Minecraft minecraft) {
        super(minecraft, parent.width, parent.height - 52, 20, 40);
        this.parent = parent;

        // contents
        for (BaseContent knownContent : parent.getBaseContents()) {
            if (knownContent.isHidden() && !Config.getInstance().dynamicRepoIsIgnoreHiddenContentFlag()) {
                continue;
            }
            var v = new ContentsList.BaseContentEntry(knownContent);
            this.addEntry(v);
        }

        // enums
        for (BaseEnum anEnum : parent.getBaseEnum()) {
            var v = new ContentsList.EnumContentEntry(anEnum);
            this.addEntry(v);
        }
    }

    protected int scrollBarX() {
        return super.scrollBarX() + 15;
    }

    @Override
    public int getRowWidth() {
        return super.getRowWidth() + 32;
    }

    public void refreshAll() {
        children().forEach(ContentEntry::refresh);
    }

    /**
     * Row with BaseContent
     */
    public class BaseContentEntry extends ContentEntry {
        private final BaseContent content;

        private BaseContentEntry(BaseContent knownContent) {
            this.content = knownContent;

            this.stateButton = createStateButton();
            stateButton.active = !content.isRequired();
            if (!stateButton.active) {
                this.stateButton.setTooltip(Tooltip.create(Component.translatable("dynamicpack.screen.pack_contents.state.tooltip_disabled")));
            }
        }

        private Button createStateButton() {
            return Button.builder(Component.translatable("dynamicpack.screen.pack_contents.state", currentState()), (button) -> clicked()).bounds(0, 0, 140, 20).build();
        }

        private void clicked() {
            try {
                content.nextOverride(parent.getBaseContents());
            } catch (Exception e) {
                Out.error("Error while content.nextOverride() in gui", e);
            }
            parent.onAfterChange();
            refreshAll();
        }


        @Override
        public void refresh() {
            stateButton.setMessage(Component.translatable("dynamicpack.screen.pack_contents.state", currentState()));
        }

        private Component currentState() {
            String s = switch (content.getOverride()) {
                case TRUE -> "dynamicpack.screen.pack_contents.state.true";
                case FALSE -> "dynamicpack.screen.pack_contents.state.false";
                case NOT_SET -> {
                    if (content.getDefaultState()) {
                        yield "dynamicpack.screen.pack_contents.state.default.true";
                    } else {
                        yield "dynamicpack.screen.pack_contents.state.default.false";
                    }
                }
            };
            return Component.translatable(s);
        }

        public void render(GuiGraphics context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            String txt = content.getId();
            String name = content.getName();
            if (name != null) {
                txt = name;
            }
            Component text = Component.literal(txt);
            context.drawString(ContentsList.this.minecraft.font, text, (x - 50), y+10, 0xFFFFFFFF);
            this.stateButton.setX(x+entryWidth-140);
            this.stateButton.setY(y);
            this.stateButton.render(context, mouseX, mouseY, tickDelta);
        }
    }


    /**
     * Row with BaseEnum
     */
    public class EnumContentEntry extends ContentEntry {
        private final BaseEnum baseEnum;


        private EnumContentEntry(BaseEnum baseEnum) {
            this.baseEnum = baseEnum;
            this.stateButton = createStateButton();
        }

        private Button createStateButton() {
            return Button.builder(currentState(), (button) -> clicked()).bounds(0, 0, 140, 20).build();
        }

        private void clicked() {
            try {
                baseEnum.applyNext(parent.getBaseContents());
            } catch (Exception e) {
                Out.error("Error while applyNext (gui)", e);
            }
            parent.onAfterChange();
            refreshAll();
        }

        @Override
        public void refresh() {
            stateButton.setMessage(currentState());
        }

        private Component currentState() {
            return Component.literal(baseEnum.getCurrentState(parent.getBaseContents()));
        }

        public void render(GuiGraphics context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            String txt = baseEnum.getId();
            String name = baseEnum.getName();
            if (name != null) {
                txt = name;
            }
            Component text = Component.literal(txt);
            context.drawString(ContentsList.this.minecraft.font, text, (x - 50), y+10, 0xFFFFFFFF, false);
            this.stateButton.setX(x+entryWidth-140);
            this.stateButton.setY(y);
            this.stateButton.render(context, mouseX, mouseY, tickDelta);
        }
    }

    public abstract static class ContentEntry extends Entry<ContentEntry> {
        protected Button stateButton;

        public abstract void refresh();

        public @NotNull List<? extends GuiEventListener> children() {
            return ImmutableList.of(this.stateButton);
        }

        public @NotNull List<? extends NarratableEntry> narratables() {
            return ImmutableList.of(this.stateButton);
        }
    }
}
