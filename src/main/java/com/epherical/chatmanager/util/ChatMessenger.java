package com.epherical.chatmanager.util;

import com.epherical.chatmanager.placeholders.PlaceHolderManager;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

/**
 * Convenience helper: expand placeholders, parse <comp> markup,
 * and send the finished MutableComponent to the player.
 */
public final class ChatMessenger {

    /**
     * @param player  player who will receive the message (null = console/broadcast)
     * @param rawText text containing {namespace:id} placeholders and <comp> tags
     */
    public static void send(ServerPlayer player, String rawText) {
        MutableComponent component = parse(player, rawText);

        // 3. deliver
        if (player != null) {
            player.sendSystemMessage(component);
        } else {
            // fall back to console
            System.out.println(component.getString());
        }
    }

    public static MutableComponent parse(ServerPlayer player, String rawText) {
        // 1. placeholder expansion
        String expanded = PlaceHolderManager.process(rawText, PlaceHolderContext.create(player.getServer(), player.serverLevel(), player));

        // 2. parse into a proper Minecraft component
        return ComponentParser.parse(expanded);
    }

    private ChatMessenger() {
    }
}
