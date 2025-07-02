package com.epherical.chatmanager.client.events;

import com.epherical.chatmanager.ChatManager;
import com.epherical.chatmanager.client.widgets.ChannelButtonWidget;
import com.epherical.chatmanager.mixin.client.ChatScreenAccessorMixin;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ScreenListener {

    private static final Component ALL_CHANNEL = Component.literal("All");


    private static ChannelEntry currentChannel = null; // Keeps track of the current channel

    private final List<ChannelEntry> allChannels = new ArrayList<>();
    private final List<ChannelButtonWidget> barButtons = new ArrayList<>();

    private ChannelButtonWidget allChannelButton = null;
    private ChannelButtonWidget moreButton = null;
    private boolean showDropdown = false;
    private int yAboveInput = 0;

    private void rebuildBar(Minecraft mc) {
        barButtons.clear();
        moreButton = null;
        allChannelButton = null;

        int minButtonWidth = 30;
        int paddingX = 10;
        int buttonSpacing = 2;
        int buttonHeight = 14;
        int xStart = 2;

        // BAR BUTTONS (0–2)
        int count = Math.min(3, allChannels.size());
        int currentX = xStart;

        int textWidth = mc.font.width(ALL_CHANNEL);
        int width = Math.max(minButtonWidth, textWidth + paddingX * 2);
        this.allChannelButton = new ChannelButtonWidget(currentX, yAboveInput, width, buttonHeight,
                new ChannelEntry(ALL_CHANNEL, null));
        allChannelButton.onClick = () -> {
            currentChannel = null;
            ChatListener.manager.setCurrentChannel(null);
        };
        currentX += width + buttonSpacing;


        for (int i = 0; i < count; i++) {
            ChannelEntry name = allChannels.get(i);
            textWidth = mc.font.width(name.component);
            width = Math.max(minButtonWidth, textWidth + paddingX * 2);
            ChannelButtonWidget widget = new ChannelButtonWidget(currentX, yAboveInput, width, buttonHeight, name);
            final int channelIndex = i;
            widget.onClick = () -> {
                ChannelEntry c = allChannels.remove(channelIndex);
                allChannels.add(0, c);
                showDropdown = false;
                currentChannel = c; // Set current channel when clicked
                ChatListener.manager.setCurrentChannel(c.key);
                rebuildBar(mc);

                // Send join command for channel when button is clicked
                String channelName = c.component.getString();
                String commandChannelName = channelName.replace(" ", "_"); // Must match your command convention
                if (mc.player != null && mc.player.connection != null) {
                    mc.player.connection.sendCommand("join " + commandChannelName);
                }
            };
            barButtons.add(widget);
            currentX += width + buttonSpacing;
        }

        // HAMBURGER BUTTON (index 3 if needed)
        if (allChannels.size() > 3) {
            int w = Math.max(minButtonWidth, mc.font.width(Component.literal("...")) + paddingX * 2);
            int right = mc.getWindow().getGuiScaledWidth() - w - 10;
            moreButton = new ChannelButtonWidget(right, yAboveInput, w, buttonHeight, new ChannelEntry(Component.literal("..."), null));
            moreButton.onClick = () -> showDropdown = !showDropdown;
            barButtons.add(moreButton);

            // DROPDOWN BUTTONS (indices 4+)
            int menuCount = allChannels.size() - 3;

            // 1. Find widest dropdown channel
            int maxDropdownWidth = minButtonWidth;
            for (int i = 3; i < allChannels.size(); i++) {
                textWidth = mc.font.width(allChannels.get(i).component);
                width = textWidth + paddingX * 2;
                if (width > maxDropdownWidth) maxDropdownWidth = width;
            }

            int dropdownX = right - maxDropdownWidth;
            if (dropdownX < 0) dropdownX = 0;
            int dropdownY = moreButton.getY() - (buttonHeight + 1) * menuCount;
            for (int i = 3; i < allChannels.size(); i++) {
                ChannelEntry name = allChannels.get(i);
                ChannelButtonWidget widget = new ChannelButtonWidget(dropdownX, dropdownY, maxDropdownWidth, buttonHeight, name);
                final int channelIndex = i;
                widget.onClick = () -> {
                    ChannelEntry c = allChannels.remove(channelIndex);
                    allChannels.add(0, c);
                    showDropdown = false;
                    currentChannel = c;
                    ChatListener.manager.setCurrentChannel(c.key);
                    rebuildBar(mc);

                    // Send join command for channel when button is clicked (dropdown)
                    String channelName = c.component.getString();
                    String commandChannelName = channelName.replace(" ", "_");
                    if (mc.player != null && mc.player.connection != null) {
                        mc.player.connection.sendCommand("join " + commandChannelName);
                    }
                };
                barButtons.add(widget);
                dropdownY += buttonHeight + 1;
            }
        }
    }

    @SubscribeEvent
    public void onScreenRender(ScreenEvent.Render.Post event) {
        if (event.getScreen() instanceof ChatScreen screen) {
            Minecraft mc = Minecraft.getInstance();

            GuiGraphics guiGraphics = event.getGuiGraphics();
            double mouseX = mc.mouseHandler.xpos() * mc.getWindow().getGuiScaledWidth() / (double) mc.getWindow().getScreenWidth();
            double mouseY = mc.mouseHandler.ypos() * mc.getWindow().getGuiScaledHeight() / (double) mc.getWindow().getScreenHeight();

            allChannelButton.render(guiGraphics, (int) mouseX, (int) mouseY, 0f);
            allChannelButton.setHovered(allChannelButton.isMouseOver(mouseX, mouseY));

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

            if (allChannelButton.mouseClicked(mouseX, mouseY, button)) {
                event.setCanceled(true);
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


            RegistryAccess registryAccess = mc.level != null ? mc.level.registryAccess() : null;
            if (registryAccess != null) {
                registryAccess.registryOrThrow(Registries.CHAT_TYPE).keySet().stream()
                        .filter(key -> key.getNamespace().equals(ChatManager.MODID)) // Only "chatmanager" chat types
                        .forEach(key -> {
                            String rawName = key.getPath();
                            // Format: replace underscores with space, capitalize each word
                            String formatted = Arrays.stream(rawName.split("_"))
                                    .map(word -> word.isEmpty() ? word : word.substring(0, 1).toUpperCase() + word.substring(1))
                                    .collect(Collectors.joining(" "));
                            allChannels.add(new ChannelEntry(Component.literal(formatted), ResourceKey.create(Registries.CHAT_TYPE, key)));
                        });
            }

            // Move the currentChannel to the front if it exists in the list
            if (currentChannel != null) {
                int idx = -1;
                for (int i = 0; i < allChannels.size(); i++) {
                    if (allChannels.get(i).component.getString().equals(currentChannel.component.getString())) {
                        idx = i;
                        break;
                    }
                }
                if (idx > 0) {
                    ChannelEntry temp = allChannels.remove(idx);
                    allChannels.add(0, temp);
                }
            }

            rebuildBar(mc);
        }
    }

    public record ChannelEntry(Component component, ResourceKey<ChatType> key) {
    }


}
