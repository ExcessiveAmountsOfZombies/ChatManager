package com.epherical.chatmanager.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.network.chat.ChatType;

import java.util.HashMap;
import java.util.Map;

public class ClientChannelManager {
    // Map ChatType resource keys to ChatComponent instances
    private final Map<ResourceKey<ChatType>, ChatComponent> channelChatComponents = new HashMap<>();

    private ResourceKey<ChatType> currentChannel = null; // null or a special value for "All"

    public ChatComponent getChatComponent(ResourceKey<ChatType> chatTypeKey) {
        // Lazily create if absent
        return channelChatComponents.get(chatTypeKey);
    }

    public void addChannel(ResourceKey<ChatType> chatTypeKey) {
        if (!channelChatComponents.containsKey(chatTypeKey)) {
            channelChatComponents.put(chatTypeKey, new ChatComponent(Minecraft.getInstance()));
        }
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
}
