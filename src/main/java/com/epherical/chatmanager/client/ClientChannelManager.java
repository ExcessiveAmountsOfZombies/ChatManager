package com.epherical.chatmanager.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.network.chat.ChatType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class ClientChannelManager {
    // Map ChatType resource keys to ChatComponent instances
    private final Map<ResourceKey<ChatType>, ChannelEntry> channelChatComponents = new HashMap<>();

    private ResourceKey<ChatType> currentChannel = null; // null or a special value for "All"

    public ChatComponent getChatComponent(ResourceKey<ChatType> chatTypeKey) {
        // Lazily create if absent
        return channelChatComponents.get(chatTypeKey).getChatComponent();
    }

    public void addChannel(ResourceKey<ChatType> chatTypeKey) {
        if (!channelChatComponents.containsKey(chatTypeKey)) {
            String rawName = chatTypeKey.location().getPath();
            // Format: replace underscores with space, capitalize each word
            String formatted = Arrays.stream(rawName.split("_"))
                    .map(word -> word.isEmpty() ? word : word.substring(0, 1).toUpperCase() + word.substring(1))
                    .collect(Collectors.joining(" "));
            channelChatComponents.put(chatTypeKey, new ChannelEntry(Component.literal(formatted), chatTypeKey, new ChatComponent(Minecraft.getInstance())));
        }
    }

    public Map<ResourceKey<ChatType>, ChannelEntry> getChannelChatComponents() {
        return channelChatComponents;
    }

    public void clearChannels() {
        channelChatComponents.clear();
    }

    public boolean hasChannel(ResourceKey<ChatType> chatTypeKey) {
        return channelChatComponents.containsKey(chatTypeKey);
    }

    public void setCurrentChannel(ResourceKey<ChatType> channel) {
        this.currentChannel = channel;
    }
    public ResourceKey<ChatType> getCurrentChannel() {
        return this.currentChannel;
    }

    public boolean isFilteringAll() {
        return currentChannel == null;
    }

    public void incrementUnread(ResourceKey<ChatType> key) {
        if (!isFilteringAll() && !Objects.equals(key, currentChannel)) {
            ChannelEntry e = channelChatComponents.get(key);
            if (e != null) e.incrementUnread();
        }
    }

    public void resetUnread() {
        channelChatComponents.values().forEach(ChannelEntry::resetUnread);
    }


}
