package com.epherical.chatmanager.listener;

import com.epherical.chatmanager.ChatManager;
import com.epherical.chatmanager.chat.Channel;
import com.epherical.chatmanager.compat.placeholders.FTBRanksPlaceholders;
import com.epherical.chatmanager.compat.placeholders.LuckPermsPlaceholders;
import com.epherical.chatmanager.config.ChatConfig;
import com.epherical.chatmanager.event.BoundChatTypeEvent;
import com.epherical.chatmanager.permissions.ChannelPermissions;
import com.epherical.chatmanager.placeholders.PlaceHolderManager;
import com.epherical.chatmanager.util.ChatMessenger;
import com.epherical.chatmanager.util.PlaceHolderContext;
import com.mojang.logging.LogUtils;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.server.permission.PermissionAPI;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;
import org.slf4j.Logger;

@EventBusSubscriber(modid = ChatManager.MODID, bus = EventBusSubscriber.Bus.GAME, value = {Dist.DEDICATED_SERVER})
public class ServerEvents {


    private static final Logger LOGGER = LogUtils.getLogger();

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public static void onServerStarting(ServerStartedEvent event) {
        if (ModList.get().isLoaded("luckperms")) {
            try {
                LuckPerms luckPermsApi = LuckPermsProvider.get();
                LuckPermsPlaceholders luckPermsPlaceholders = new LuckPermsPlaceholders(luckPermsApi);
            } catch (Exception e) {
                LOGGER.warn("Are you loading LuckPerms? in Single Player? That is not a supported environment.", e);
                LOGGER.warn(e.getMessage());
            }

            LOGGER.info("[ChatManager] LuckPerms placeholders initialized");
        }
        if (ModList.get().isLoaded("ftbranks")) {
            FTBRanksPlaceholders ftbranksPlaceholders = new FTBRanksPlaceholders();
            LOGGER.info("[ChatManager] FTBRanks placeholders initialized");
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

        /*String rawMsg = "<comp color='#43b581' style='bold'>Welcome, </comp>" +
                "<comp color='#faa61a'>{chatmanager:greet,Steve}</comp>" +
                "<comp color='#ffffff'>! Your coordinates: </comp>" +
                "<comp color='#55acee' style='italic'>{chatmanager:coords}</comp>";*/
        //ChatMessenger.send(event.getPlayer(), rawMsg);


        //event.getPlayer().sendSystemMessage(processed);



        ServerPlayer player = event.getPlayer();

        Channel channel = ChatManager.mod.getChannelManager().getChannel(player);
        event.setMessage(channel.parseMessage(player, player.getUUID(), event.getUsername(), event.getRawText()));
        //event.setCanceled(true);
    }


    /*@SubscribeEvent
    public static void onServerChat(MessagedParsedEvent event) {
        MinecraftServer server = event.getPlayer().getServer();

        PlayerChatMessage unsigned = PlayerChatMessage.unsigned(event.getUuid(), "");

        PlayerChatMessage playerChatMessage = unsigned.withUnsignedContent(event.getMessage());

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            MessageSendEvent post = NeoForge.EVENT_BUS.post(new MessageSendEvent(event.getPlayer(), player, event.getMessage()));
            if (!post.isCanceled()) {

                player.sendChatMessage(OutgoingChatMessage.create(playerChatMessage), false, ChatType.bind(event.getChannel().getChatTypeKey(), player));
            }
        }
    }*/

    @SubscribeEvent
    public static void onBoundChatType(BoundChatTypeEvent e) {
        Channel channel = ChatManager.mod.getChannelManager().getChannel(e.getSender());
        e.setBoundChatType(ChatType.bind(channel.getChatTypeKey(), e.getSender()));
    }

}
