package com.calculatorsteam.dynamicpack.mixins;

import com.calculatorsteam.dynamicpack.client.gui.widget.DynamicPackResourcePackEntryWidget;
import com.calculatorsteam.dynamicpack.client.gui.widget.ResourcePackEntryWidget;
import com.calculatorsteam.dynamicpack.platform.VersionFunctions;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.packs.TransferableSelectionList;
import net.minecraft.client.gui.screens.packs.PackSelectionModel;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TransferableSelectionList.PackEntry.class)
public abstract class ResourcePackEntryMixin/*? if >=1.21.9 {*/ extends net.minecraft.client.gui.components.ObjectSelectionList.Entry<TransferableSelectionList.Entry>/*?}*/ {

    @Shadow
    protected abstract boolean showHoverOverlay();

    @Shadow
    @Final
    private PackSelectionModel.Entry pack;
    @Shadow
    @Final
    private TransferableSelectionList parent;

    @Unique
    int dynamicpack$selected = -1;
    @Unique
    private Boolean fold;
    @Unique
    int dynamicpack$foldTicks = 0;
    @Unique
    private static final int maxFoldTicks = 10;
    @Unique
    private static final ResourcePackEntryWidget DYNAMIC_WIDGET = new DynamicPackResourcePackEntryWidget();

    @Inject(at = @At("TAIL"),
            /*? if >=1.21.9 {*/
            method = "renderContent"
            /*?} else {*/
            /*method = "render(Lnet/minecraft/client/gui/GuiGraphics;IIIIIIIZF)V"
            *//*?}*/
    )
    private void render(GuiGraphics context, /*? if <1.21.9 {*//*int index, int y, int x, int entryWidth, int entryHeight,*//*?}*/ int mouseX, int mouseY, boolean hovered, float tickDelta, CallbackInfo info) {
        //? if >=1.21.9
        final int x = this.getX(), y = this.getY() + 2, entryWidth = this.getWidth(), entryHeight = this.getHeight();
        int prevMargin = 0;
        /*? if >=1.21 {*/
        int deltaX = 3 + (VersionFunctions.maxScrollAmount(parent) > 0 ? 7 : 0);
        /*?} else {*/
        /*int deltaX = 8 + (parent.getMaxScroll() > 0 ? 7 : 0);
        *//*?}*/
        boolean selectable = showHoverOverlay();
        dynamicpack$selected = -1;
        if (dynamicpack$foldTicks == maxFoldTicks) {
            /*? if >=1.21.11 {*/
            VersionFunctions.blitSprite(context, net.minecraft.resources.Identifier.withDefaultNamespace("transferable_list/unselect"),
                    x + entryWidth - 16 - deltaX, y + entryWidth / 20 - 16, 16, 32);
            /*?} else if >=1.21 {*/
            /*VersionFunctions.blitSprite(context, net.minecraft.resources.ResourceLocation.withDefaultNamespace("transferable_list/unselect"),
                    x + entryWidth - 16 - deltaX, y + entryWidth / 20 - 16, 16, 32);
            *//*?} else {*/
            /*context.blit(new net.minecraft.resources.ResourceLocation("textures/gui/resource_packs.png"),
                    x + entryWidth - 16 - deltaX, y + entryWidth / 20 - 16, 32.0F, 0.0F, 16, 32, 256, 256);
            *//*?}*/
        } else {
            ResourcePackEntryWidget widget = DYNAMIC_WIDGET;
            if (widget.isVisible(pack, selectable)) {
                /*? if >=1.21.9 {*/
                deltaX = Math.max(prevMargin, widget.getXMargin(pack));
                /*?} else {*/
                /*deltaX += Math.max(prevMargin, widget.getXMargin(pack));
                *//*?}*/
                int width = widget.getWidth(pack);
                int height = widget.getHeight(pack, entryHeight);
                int entryX = x + entryWidth - (deltaX + width) * (maxFoldTicks - dynamicpack$foldTicks) / maxFoldTicks;
                int entryY = y + widget.getY(pack, entryHeight);
                deltaX += width;
                boolean widgetHovered = mouseX <= entryX + width && mouseX >= entryX &&
                        mouseY <= entryY + height && mouseY >= entryY;
                widget.render(pack, context, entryX, entryY, widgetHovered, tickDelta);
                if (widgetHovered) {
                    dynamicpack$selected = 0; // фиксированный индекс
                    //? if > 1.21.9
                    context.requestCursor(parent.isActive() ? com.mojang.blaze3d.platform.cursor.CursorTypes.POINTING_HAND : com.mojang.blaze3d.platform.cursor.CursorTypes.NOT_ALLOWED);
                }
                prevMargin = widget.getXMargin(pack);
            }
        }
        if (fold == null) fold = deltaX > 48;
        if (!fold) return;
        int mouseRange = Math.max(10, deltaX) + 40;
        if (mouseX >= x + entryWidth - mouseRange && mouseX <= x + entryWidth &&
                mouseY <= y + entryHeight && mouseY >= y)
            dynamicpack$foldTicks = Math.max(dynamicpack$foldTicks - 1, 0);
        else
            dynamicpack$foldTicks = Math.min(dynamicpack$foldTicks + 1, maxFoldTicks);
    }

    /*? if >=1.21.9 {*/
    @Inject(method = "mouseClicked", at = @At("RETURN"), cancellable = true)
    private void dynamicpack$afterMouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean isDoubleClick, CallbackInfoReturnable<Boolean> cir) {
    /*?} else {*/
    /*@Inject(method = "mouseClicked(DDI)Z", at = @At("RETURN"), cancellable = true)
    private void dynamicpack$afterMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
    *//*?}*/
        if (dynamicpack$selected != -1) {
            cir.setReturnValue(false);
            DYNAMIC_WIDGET.onClick(pack);
        }
    }
}