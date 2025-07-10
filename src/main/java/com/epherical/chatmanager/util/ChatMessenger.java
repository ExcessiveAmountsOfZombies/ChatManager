package com.epherical.chatmanager.util;

import com.epherical.chatmanager.placeholders.PlaceHolderManager;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

/**
 * Convenience helper: expand placeholders, parse &lt;comp&gt; markup,
 * and send the finished MutableComponent to the player.
 */
public final class ChatMessenger {

    /**
     * @param player  player who will receive the message (null = console/broadcast)
     * @param rawText text containing {namespace:id} placeholders and &lt;comp&gt; tags
     */
    public static void send(ServerPlayer player, String rawText) {
        MutableComponent component = parse(player, rawText);

        // 2. deliver
        if (player != null) {
            player.sendSystemMessage(component);
        } else {
            // fall back to console
            System.out.println(component.getString());
        }
    }

    public static MutableComponent parse(ServerPlayer player, String rawText) {
        return PlaceHolderManager.process(
                rawText,
                PlaceHolderContext.create(
                        player.getServer(),
                        player.serverLevel(),
                        player
                )
        );
    }

    private ChatMessenger() {
    }
}
