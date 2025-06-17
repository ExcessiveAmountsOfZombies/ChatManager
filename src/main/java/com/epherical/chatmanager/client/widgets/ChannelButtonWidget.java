package com.epherical.chatmanager.client.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

public class ChannelButtonWidget extends AbstractWidget {
    public Runnable onClick = null;

    public ChannelButtonWidget(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int color = this.isHovered() ? 0x99FFFFFF : 0x99000000; // Hover highlight
        guiGraphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), color);



        int textColor = 0xFFFFFF;
        Minecraft mc = Minecraft.getInstance();
        int textWidth = mc.font.width(getMessage());
        int textX = getX() + (getWidth() - textWidth) / 2;
        int textY = getY() + (getHeight() - mc.font.lineHeight) / 2 + 1;
        guiGraphics.drawString(mc.font, getMessage(), textX, textY, textColor);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        // Play Minecraft click sound
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            playDownSound(mc.getSoundManager());
        }
        if (onClick != null) {
            onClick.run();
        }
    }


    public void setHovered(boolean hovered) {
        isHovered = hovered;
    }


    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }
}
