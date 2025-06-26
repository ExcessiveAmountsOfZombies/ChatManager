package com.epherical.chatmanager.client.events;

import com.epherical.chatmanager.client.widgets.ChannelButtonWidget;
import com.epherical.chatmanager.mixin.ChatScreenAccessorMixin;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;

import java.util.ArrayList;
import java.util.List;

public class ScreenListener {

    private final List<Component> allChannels = new ArrayList<>();
    private final List<ChannelButtonWidget> barButtons = new ArrayList<>();
    private ChannelButtonWidget moreButton = null;
    private boolean showDropdown = false;
    private int yAboveInput = 0;

    private void rebuildBar(Minecraft mc) {
        barButtons.clear();
        moreButton = null;

        int minButtonWidth = 30;
        int paddingX = 10;
        int buttonSpacing = 2;
        int buttonHeight = 14;
        int xStart = 2;

        // BAR BUTTONS (0–2)
        int count = Math.min(3, allChannels.size());
        int currentX = xStart;
        for (int i = 0; i < count; i++) {
            Component name = allChannels.get(i);
            int textWidth = mc.font.width(name);
            int width = Math.max(minButtonWidth, textWidth + paddingX * 2);
            ChannelButtonWidget widget = new ChannelButtonWidget(currentX, yAboveInput, width, buttonHeight, name);
            final int channelIndex = i;
            widget.onClick = () -> {
                Component c = allChannels.remove(channelIndex);
                allChannels.add(0, c);
                showDropdown = false;
                rebuildBar(mc);
            };
            barButtons.add(widget);
            currentX += width + buttonSpacing;
        }

        // HAMBURGER BUTTON (index 3 if needed)
        if (allChannels.size() > 3) {
            int w = Math.max(minButtonWidth, mc.font.width(Component.literal("...")) + paddingX * 2);
            int right = mc.getWindow().getGuiScaledWidth() - w - 10;
            moreButton = new ChannelButtonWidget(right, yAboveInput, w, buttonHeight, Component.literal("..."));
            moreButton.onClick = () -> showDropdown = !showDropdown;
            barButtons.add(moreButton);

            // DROPDOWN BUTTONS (indices 4+)
            int menuCount = allChannels.size() - 3;

            // 1. Find widest dropdown channel
            int maxDropdownWidth = minButtonWidth;
            for (int i = 3; i < allChannels.size(); i++) {
                int textWidth = mc.font.width(allChannels.get(i));
                int width = textWidth + paddingX * 2;
                if (width > maxDropdownWidth) maxDropdownWidth = width;
            }

            int dropdownX = right - maxDropdownWidth;
            if (dropdownX < 0) dropdownX = 0;
            int dropdownY = moreButton.getY() - (buttonHeight + 1) * menuCount;
            for (int i = 3; i < allChannels.size(); i++) {
                Component name = allChannels.get(i);
                ChannelButtonWidget widget = new ChannelButtonWidget(dropdownX, dropdownY, maxDropdownWidth, buttonHeight, name);
                final int channelIndex = i;
                widget.onClick = () -> {
                    Component c = allChannels.remove(channelIndex);
                    allChannels.add(0, c);
                    showDropdown = false;
                    rebuildBar(mc);
                };
                barButtons.add(widget);
                dropdownY += buttonHeight + 1;
            }
        }
    }

    @SubscribeEvent
    public void onScreenRender(ScreenEvent.Render.Post event) {
        if (event.getScreen() instanceof ChatScreen) {
            Minecraft mc = Minecraft.getInstance();
            GuiGraphics guiGraphics = event.getGuiGraphics();
            double mouseX = mc.mouseHandler.xpos() * mc.getWindow().getGuiScaledWidth() / (double) mc.getWindow().getScreenWidth();
            double mouseY = mc.mouseHandler.ypos() * mc.getWindow().getGuiScaledHeight() / (double) mc.getWindow().getScreenHeight();

            // Render bar (0–2) and hamburger (3)
            int barEnd = Math.min(4, barButtons.size());
            for (int i = 0; i < barEnd; i++) {
                ChannelButtonWidget button = barButtons.get(i);
                button.setHovered(button.isMouseOver(mouseX, mouseY));
                button.render(guiGraphics, (int) mouseX, (int) mouseY, 0f);
            }
            // Dropdown (4+)
            if (showDropdown && barButtons.size() > 4) {
                int x = barButtons.get(4).getX();
                int y = barButtons.get(4).getY();
                int width = barButtons.get(4).getWidth();
                int height = (barButtons.size() - 4) * (barButtons.get(4).getHeight() + 1);
                guiGraphics.fill(x - 2, y - 2, x + width + 2, y + height + 2, 0xCC222222);
                for (int i = 4; i < barButtons.size(); i++) {
                    ChannelButtonWidget drop = barButtons.get(i);
                    drop.setHovered(drop.isMouseOver(mouseX, mouseY));
                    drop.render(guiGraphics, (int) mouseX, (int) mouseY, 0f);
                }
            }
        }
    }

    @SubscribeEvent
    public void onMouseClicked(ScreenEvent.MouseButtonPressed.Pre event) {
        if (event.getScreen() instanceof ChatScreen) {
            double mouseX = event.getMouseX();
            double mouseY = event.getMouseY();
            int button = event.getButton();

            // Dropdown (4+)
            if (showDropdown && barButtons.size() > 4) {
                boolean insideDropdown = false;
                for (int i = 4; i < barButtons.size(); i++) {
                    ChannelButtonWidget widget = barButtons.get(i);
                    if (widget.mouseClicked(mouseX, mouseY, button)) {
                        event.setCanceled(true);
                        return;
                    }
                    if (widget.isMouseOver(mouseX, mouseY)) insideDropdown = true;
                }
                // Hamburger closes
                if (barButtons.size() > 3 && barButtons.get(3).isMouseOver(mouseX, mouseY) && button == 0) {
                    barButtons.get(3).onClick(mouseX, mouseY);
                    event.setCanceled(true);
                    return;
                }
                // Click outside closes
                if (!insideDropdown && (!barButtons.get(3).isMouseOver(mouseX, mouseY))) {
                    showDropdown = false;
                    event.setCanceled(true);
                }
                return;
            }

            // Normal bar
            int barEnd = Math.min(4, barButtons.size());
            for (int i = 0; i < barEnd; i++) {
                ChannelButtonWidget widget = barButtons.get(i);
                if (widget.mouseClicked(mouseX, mouseY, button)) {
                    event.setCanceled(true);
                    return;
                }
            }
        }
    }

    @SubscribeEvent
    public void onScreenInit(ScreenEvent.Init.Post event) {
        if (event.getScreen() instanceof ChatScreen chatScreen) {
            Minecraft mc = Minecraft.getInstance();
            EditBox input = ((ChatScreenAccessorMixin) chatScreen).getInput();
            yAboveInput = input.getY() - 12 - 6;

            // Example channel initialization
            allChannels.clear();
            allChannels.add(Component.literal("Global"));
            allChannels.add(Component.literal("Team"));
            allChannels.add(Component.literal("Staff"));
            allChannels.add(Component.literal("Builder"));
            allChannels.add(Component.literal("Market"));
            allChannels.add(Component.literal("RP")); // Additional as needed

            rebuildBar(mc);
        }
    }
}
