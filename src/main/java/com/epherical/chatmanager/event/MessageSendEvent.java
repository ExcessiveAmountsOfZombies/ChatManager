package com.epherical.chatmanager.event;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

public class MessageSendEvent extends Event implements ICancellableEvent {

    private final ServerPlayer fromPlayer;
    private final ServerPlayer toPlayer;
    private final MutableComponent message;


    public MessageSendEvent(ServerPlayer fromPlayer, ServerPlayer toPlayer, MutableComponent message) {
        this.fromPlayer = fromPlayer;
        this.toPlayer = toPlayer;
        this.message = message;
    }

    public ServerPlayer getFromPlayer() {
        return fromPlayer;
    }

    public ServerPlayer getToPlayer() {
        return toPlayer;
    }

    public MutableComponent getMessage() {
        return message;
    }
}
