package com.epherical.chatmanager.event;

import com.epherical.chatmanager.chat.Channel;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

import java.util.UUID;

public class MessagedParsedEvent extends Event implements ICancellableEvent {

    private final ServerPlayer player;
    private final String rawText;
    private final String username;
    private final UUID uuid;
    private final Channel channel;
    private MutableComponent message;


    public MessagedParsedEvent(ServerPlayer player, UUID uuid, String username, String rawText, Channel channel, MutableComponent message) {
        this.player = player;
        this.uuid = uuid;
        this.rawText = rawText;
        this.username = username;
        this.message = message;
        this.channel = channel;
    }

    public UUID getUuid() {
        return uuid;
    }

    public Channel getChannel() {
        return channel;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public String getRawText() {
        return rawText;
    }

    public String getUsername() {
        return username;
    }

    public MutableComponent getMessage() {
        return message;
    }

    public void setMessage(MutableComponent message) {
        this.message = message;
    }
}
