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


    public ChannelEntry getEntry() {
        return entry;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Select different background if this is the *selected* channel
        int selectedColor = 0xCC3264E2; // Example: bold blueish highlight
        int hoverColor = 0x99FFFFFF;    // Hover highlight
        int normalColor = 0x99000000;   // Normal/idle

        boolean isSelected = entry.isSelected(); // You will need to implement or provide this logic!
        int color = isSelected ? selectedColor : (this.isHovered() ? hoverColor : normalColor);
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

            float scale = 0.75f;
            int baseBadgeWidth = mc.font.width(badge) + 8;
            int baseBadgeHeight = mc.font.lineHeight;
            int badgeW = (int) (baseBadgeWidth * scale);
            int badgeH = (int) (baseBadgeHeight * scale);

            int horizontalPadding = 3;
            int verticalPadding = 3;

            int bx = getX() + getWidth() - badgeW + horizontalPadding;
            int by = getY() - verticalPadding;

            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(bx, by, 0);
            guiGraphics.pose().scale(scale, scale, 1);

            guiGraphics.fill(0, 0, baseBadgeWidth, baseBadgeHeight, 0xCCFF4444);

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
