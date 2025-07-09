package com.epherical.chatmanager.mixin;

import com.epherical.chatmanager.event.BoundChatTypeEvent;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.FilterMask;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.players.PlayerList;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Redirects the *delegate* call inside
 * PlayerList#broadcastChatMessage(PlayerChatMessage, ServerPlayer, ChatType.Bound)
 * so that the bound chat type can be replaced through a NeoForge event.
 */
@Mixin(PlayerList.class)
public abstract class PlayerListMixin {

    /**
     * Original byte-code (simplified):
     *
     *     this.broadcastChatMessage(message,
     *                               sender::shouldFilterMessageTo,
     *                               sender,
     *                               boundChatType);
     *
     * We intercept that call, fire a custom event, and then invoke the
     * delegate again with the (potentially) modified bound chat type.
     */
    @Redirect(
            method = "broadcastChatMessage(Lnet/minecraft/network/chat/PlayerChatMessage;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/network/chat/ChatType$Bound;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/players/PlayerList;broadcastChatMessage(Lnet/minecraft/network/chat/PlayerChatMessage;Ljava/util/function/Predicate;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/network/chat/ChatType$Bound;)V"
            )
    )
    private void chatmanager$redirectMsg(PlayerList instance,
                                         PlayerChatMessage message,
                                         Predicate<ServerPlayer> shouldFilter,
                                         ServerPlayer player,
                                         ChatType.Bound type) {


        // Fire the custom event so mods can replace / cancel the bound type.
        BoundChatTypeEvent event = new BoundChatTypeEvent(player, message, type);
        NeoForge.EVENT_BUS.post(event);

        if (event.isCanceled()) {
            return; // nothing is broadcast
        }

        ChatType.Bound finalBound = event.getBoundChatType();

        // Call the original delegate with the (possibly) modified bound type.
        chatmanager$invokeBroadcastChatMessage(message, shouldFilter, player, finalBound);
    }


    @Invoker("broadcastChatMessage")
    abstract void chatmanager$invokeBroadcastChatMessage(
            PlayerChatMessage message,
            Predicate<ServerPlayer> filterMask,
            ServerPlayer sender,
            ChatType.Bound boundChatType);

}
