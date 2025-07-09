package com.epherical.chatmanager.client;

import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;

public final class ChannelEntry {

    private final ChatComponent chatComponent;
    private final Component label;
    private final ResourceKey<ChatType> key;
    private int unread;

    public ChannelEntry(Component label, ResourceKey<ChatType> key, ChatComponent chatComponent) {
        this.label = label;
        this.key = key;
        this.chatComponent = chatComponent;
    }


    public ChatComponent getChatComponent() {
        return chatComponent;
    }

    public Component label() {
        return label;
    }

    public ResourceKey<ChatType> key() {
        return key;
    }

    public int unread() {
        return unread;
    }

    public void incrementUnread() {
        ++unread;
    }

    public void resetUnread() {
        unread = 0;
    }
}
