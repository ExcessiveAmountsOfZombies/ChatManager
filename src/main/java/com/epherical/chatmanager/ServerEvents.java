package com.epherical.chatmanager;

import com.epherical.chatmanager.chat.Channel;
import com.epherical.chatmanager.config.ChatConfig;
import com.epherical.chatmanager.event.MessageSendEvent;
import com.epherical.chatmanager.event.MessagedParsedEvent;
import com.epherical.chatmanager.permissions.ChannelPermissions;
import com.epherical.chatmanager.placeholders.LuckPermsPlaceholders;
import com.epherical.chatmanager.util.ChatMessenger;
import com.mojang.logging.LogUtils;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.server.permission.PermissionAPI;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;
import org.slf4j.Logger;

@EventBusSubscriber(modid = ChatManager.MODID, bus = EventBusSubscriber.Bus.GAME, value = {Dist.DEDICATED_SERVER})
public class ServerEvents {


    private static final Logger LOGGER = LogUtils.getLogger();

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        if (ModList.get().isLoaded("luckperms")) {
            LuckPerms luckPermsApi = LuckPermsProvider.get();
            LuckPermsPlaceholders luckPermsPlaceholders = new LuckPermsPlaceholders(luckPermsApi);

            LOGGER.info("LuckPerms placeholders initialized");
        }
    }

    // Utility method to check if a player has permission for a channel
    private static boolean canJoinChannel(ServerPlayer player, Channel channel) {
        String permission = channel.permission();
        // If there's no permission required, or OP players have all perms by default, allow them
        if (permission == null || permission.isBlank()) {
            return true;
        }

        PermissionNode<Boolean> permissionNode = ChannelPermissions.getPermissionNode(channel.name());
        return PermissionAPI.getPermission(player, permissionNode);
    }

    // Call this method when a player joins (e.g., onPlayerLoggedIn event)
    public static void joinDefaultChannel(ServerPlayer player) {
        for (Channel channel : ChatConfig.parsedChannels.values()) {
            if (canJoinChannel(player, channel)) {
                // Add player to the channel here, e.g.:
                ChatManager.mod.getChannelManager().joinChannel(player, channel.name());
                // Optionally, notify the player
                return;
            }
        }
        // If no channels were available
        ChatMessenger.send(player, "<comp color='#ff0000'>No accessible chat channels were found.</comp>");
    }


    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        joinDefaultChannel(player);
    }


    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public static void playersChatting(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();

        Channel channel = ChatManager.mod.getChannelManager().getChannel(player);

        channel.parseMessage(player, player.getUUID(), event.getUsername(), event.getRawText());
        event.setCanceled(true);
    }


    @SubscribeEvent
    public static void onServerChat(MessagedParsedEvent event) {
        MinecraftServer server = event.getPlayer().getServer();

        PlayerChatMessage unsigned = PlayerChatMessage.unsigned(event.getUuid(), "");

        PlayerChatMessage playerChatMessage = unsigned.withUnsignedContent(event.getMessage());

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            MessageSendEvent post = NeoForge.EVENT_BUS.post(new MessageSendEvent(event.getPlayer(), player, event.getMessage()));
            if (!post.isCanceled()) {
                event.getChannel();
                player.sendChatMessage(OutgoingChatMessage.create(playerChatMessage), false, ChatType.bind(event.getChannel().getChatTypeKey(), player));
            }
        }
    }
}
