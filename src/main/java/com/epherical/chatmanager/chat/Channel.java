package com.epherical.chatmanager.chat;

import com.epherical.chatmanager.ChatManager;
import com.epherical.chatmanager.event.MessagedParsedEvent;
import com.epherical.chatmanager.util.ChatMessenger;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;

import java.util.List;
import java.util.UUID;

public class Channel {

    private final ResourceKey<ChatType> chatTypeKey;
    private final String name;
    private final List<String> aliases;
    private final String permission;
    private final ChannelType type;
    private final String format;

    public Channel(String name, List<String> aliases, String permission, ChannelType type, String format) {
        this.name = name;
        this.aliases = aliases;
        this.permission = permission;
        this.type = type;
        this.format = format;
        this.chatTypeKey = makeChannelChatType(name);
    }

    public Channel(String name, List<String> aliases, String permission, String type, String format) {
        this(name, aliases, permission, ChannelType.valueOf(type), format);
    }


    public MutableComponent parseMessage(ServerPlayer player, UUID uuid, String username, String message) {
        return ChatMessenger.parse(player, format.replaceAll("\\{message}", message));
        /*// todo; see if the actual message content gets parsed...
        MessagedParsedEvent post = NeoForge.EVENT_BUS.post(new MessagedParsedEvent(player, uuid, username, message, this, parse));*/
    }

    public void sendMessage(ServerPlayer player, String username, String message) {

    }

    private static ResourceKey<ChatType> makeChannelChatType(String name) {
        return ResourceKey.create(Registries.CHAT_TYPE, ResourceLocation.fromNamespaceAndPath(ChatManager.MODID, name.toLowerCase()));
    }


    public ResourceKey<ChatType> getChatTypeKey() {
        return chatTypeKey;
    }

    public String name() {
        return name;
    }

    public List<String> aliases() {
        return aliases;
    }

    public String permission() {
        return permission;
    }

    public ChannelType type() {
        return type;
    }

    public String format() {
        return format;
    }
}
