package com.epherical.chatmanager;

import com.epherical.chatmanager.chat.Channel;
import com.epherical.chatmanager.event.MessageSendEvent;
import com.epherical.chatmanager.event.MessagedParsedEvent;
import com.epherical.chatmanager.placeholders.LuckPermsPlaceholders;
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
import net.neoforged.neoforge.event.server.ServerStartingEvent;
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
