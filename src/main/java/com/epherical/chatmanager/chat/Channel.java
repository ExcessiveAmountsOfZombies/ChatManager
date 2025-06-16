package com.epherical.chatmanager.chat;

import com.epherical.chatmanager.event.MessagedParsedEvent;
import com.epherical.chatmanager.util.ChatMessenger;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

import net.neoforged.neoforge.common.NeoForge;

import java.util.List;
import java.util.UUID;

public record Channel(String name, List<String> aliases, String permission, ChannelType type, String format) {

    public Channel(String name, List<String> aliases, String permission, ChannelType type, String format) {
        this.name = name;
        this.aliases = aliases;
        this.permission = permission;
        this.type = type;
        this.format = format;
    }

    public Channel(String name, List<String> aliases, String permission, String type, String format) {
        this(name, aliases, permission, ChannelType.valueOf(type), format);
    }


    public void parseMessage(ServerPlayer player, UUID uuid, String username, String message) {
        MutableComponent parse = ChatMessenger.parse(player, format.replaceAll("\\{message}", message));
        // todo; see if the actual message content gets parsed...
        MessagedParsedEvent post = NeoForge.EVENT_BUS.post(new MessagedParsedEvent(player, uuid, username, message, this, parse));
    }

    public void sendMessage(ServerPlayer player, String username, String message) {

    }
}
