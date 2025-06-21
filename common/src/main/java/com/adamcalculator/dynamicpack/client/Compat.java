package com.adamcalculator.dynamicpack.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

public class Compat {
    private static final Minecraft CLIENT = Minecraft.getInstance();

    public static <T extends GuiEventListener & Renderable & NarratableEntry> T createButton(Component text, Runnable press, int w, int h, int x, int y) {
        return (T) Button.builder(text, button -> press.run()).size(w, h).pos(x, y).build();
    }


    /**
     * Rewritten from ModMenu (MIT)
     */
    public static void drawWrappedString(GuiGraphics matrices, String string, int x, int y, int wrapWidth, int lines, int color) {
        while (string != null && string.endsWith("\n")) {
            string = string.substring(0, string.length() - 1);
        }
        List<FormattedText> strings = CLIENT.font.getSplitter().splitLines(Component.literal(string), wrapWidth, Style.EMPTY);
        for (int i = 0; i < strings.size(); i++) {
            if (i >= lines) {
                break;
            }
            FormattedText renderable = strings.get(i);
            if (i == lines - 1 && strings.size() > lines) {
                renderable = FormattedText.composite(strings.get(i), FormattedText.of("..."));
            }
            FormattedCharSequence line = Language.getInstance().getVisualOrder(renderable);
            int x1 = x;

            if (CLIENT.font.isBidirectional()) {
                int width = CLIENT.font.width(line);
                x1 += (float) (wrapWidth - width);
            }
            matrices.drawString(CLIENT.font, line, x1, y + i * CLIENT.font.lineHeight, color, false);
        }
    }


    public static void runAtUI(Runnable o) {
        CLIENT.execute(o);
    }

    public static void drawTexture(GuiGraphics context, ResourceLocation texture, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight) {
        RenderSystem.setShaderTexture(0, Minecraft.getInstance().getTextureManager().getTexture(texture).getTexture());
        context.blit(RenderType::guiTextured, texture, x, y, u, v, width, height, textureWidth, textureHeight);
    }

    // in 1.20.2(and later maybe) not needed
    public static void renderBackground(Screen screen, Object context, int mouseX, int mouseY, float delta) {
        screen.renderBackground((GuiGraphics) context, mouseX, mouseY, delta);
    }

    public static void drawString(Object context, Font font, Component component, int i, int i1, int i2) {
        ((GuiGraphics) context).drawString(font, component, i, i1, i2);
    }

    public static void drawCenteredString(Object context, Font font, Component title, int i, int i1, int i2) {
        ((GuiGraphics) context).drawCenteredString(font, title, i, i1, i2);
    }
}
