package com.epherical.chatmanager.event;

import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * Fired just before PlayerList actually broadcasts a chat message.
 * Handlers may:
 *   • call {@link #setBoundChatType(ChatType.Bound)} to replace the type
 */
public class BoundChatTypeEvent extends Event implements ICancellableEvent {

    private final ServerPlayer sender;
    private final PlayerChatMessage message;
    private ChatType.Bound boundChatType;

    public BoundChatTypeEvent(ServerPlayer sender,
                              PlayerChatMessage message,
                              ChatType.Bound boundChatType) {
        this.sender = sender;
        this.message = message;
        this.boundChatType = boundChatType;
    }

    /* ---------------- getters / setters ---------------- */

    public ServerPlayer getSender() {
        return sender;
    }

    public PlayerChatMessage getMessage() {
        return message;
    }

    public ChatType.Bound getBoundChatType() {
        return boundChatType;
    }

    public void setBoundChatType(ChatType.Bound boundChatType) {
        this.boundChatType = boundChatType;
    }
}
