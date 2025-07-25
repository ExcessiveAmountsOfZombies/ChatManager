package com.epherical.chatmanager.chat;

import com.epherical.chatmanager.ChatManager;
import com.epherical.chatmanager.util.ChatMessenger;
import com.epherical.epherolib.libs.org.spongepowered.configurate.ConfigurationNode;
import com.epherical.epherolib.libs.org.spongepowered.configurate.serialize.SerializationException;
import com.epherical.epherolib.libs.org.spongepowered.configurate.serialize.TypeSerializer;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.Type;
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

    public static class Serializer implements TypeSerializer<Channel> {
        public static final Serializer INSTANCE = new Serializer();

        public static final String KEY_NAME = "name";
        public static final String KEY_ALIASES = "aliases";
        public static final String KEY_PERMISSION = "permission";
        public static final String KEY_TYPE = "type";
        public static final String KEY_FORMAT = "format";


        @Override
        public Channel deserialize(Type type, ConfigurationNode node) throws SerializationException {
            String name = node.node(KEY_NAME).getString();
            if (name == null || name.isBlank()) {
                throw new SerializationException(node, Channel.class, "Channel is missing required field 'name'");
            }

            List<String> aliases = node.node(KEY_ALIASES).getList(String.class, List.of());
            String permission = node.node(KEY_PERMISSION).getString("");
            String channelType = node.node(KEY_TYPE).getString(ChannelType.LOCAL.name());
            String format = node.node(KEY_FORMAT).getString("{chatmanager:display}: <comp color='#808080'>{message}</comp>");

            return new Channel(name, aliases, permission, channelType, format);

        }

        @Override
        public void serialize(Type type, @Nullable Channel channel, ConfigurationNode node) throws SerializationException {
            if (channel == null) {
                node.raw(null);
                return;
            }

            node.node(KEY_NAME).set(channel.name());
            node.node(KEY_ALIASES).setList(String.class, channel.aliases());
            node.node(KEY_PERMISSION).set(channel.permission());
            node.node(KEY_TYPE).set(channel.type().name());
            node.node(KEY_FORMAT).set(channel.format());

        }
    }
}
