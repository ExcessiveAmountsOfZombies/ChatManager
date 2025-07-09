package com.epherical.chatmanager.client.widgets;

import com.epherical.chatmanager.client.ChannelEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;

public class ChannelButtonWidget extends AbstractWidget {
    public Runnable onClick = null;


    private final ChannelEntry entry;

    public ChannelButtonWidget(int x, int y, int width, int height, ChannelEntry message) {
        super(x, y, width, height, message.label());
        this.entry = message;
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

        /* ---------- unread badge ---------- */
        int unread = entry.unread();
        if (unread > 0) {
            String badge = unread > 9 ? "9+" : String.valueOf(unread);

            // Badge scaling and dimensions
            float scale = 0.75f;
            int baseBadgeWidth = mc.font.width(badge) + 8;
            int baseBadgeHeight = mc.font.lineHeight;
            int badgeW = (int) (baseBadgeWidth * scale);
            int badgeH = (int) (baseBadgeHeight * scale);

            // Padding from the button's edge
            int horizontalPadding = 3;
            int verticalPadding = 3;

            // Top-right anchor based on button's size and padding
            int bx = getX() + getWidth() - badgeW + horizontalPadding;
            int by = getY() - verticalPadding;

            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(bx, by, 0);
            guiGraphics.pose().scale(scale, scale, 1);

            // Draw badge background
            guiGraphics.fill(0, 0, baseBadgeWidth, baseBadgeHeight, 0xCCFF4444);

            // Draw badge text
            int badgeTextX = 4;
            int badgeTextY = 1;
            guiGraphics.drawString(mc.font, badge, badgeTextX, badgeTextY, 0xFFFFFF);

            guiGraphics.pose().popPose();
        }

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
