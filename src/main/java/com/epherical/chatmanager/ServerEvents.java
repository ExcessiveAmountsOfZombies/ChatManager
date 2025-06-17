package com.epherical.chatmanager;

import com.epherical.chatmanager.chat.Channel;
import com.epherical.chatmanager.config.ChatConfig;
import com.epherical.chatmanager.event.MessageSendEvent;
import com.epherical.chatmanager.event.MessagedParsedEvent;
import com.epherical.chatmanager.placeholders.LuckPermsPlaceholders;
import com.epherical.chatmanager.placeholders.PlaceholderManager;
import com.mojang.logging.LogUtils;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
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

import static com.epherical.chatmanager.ChatManager.DISPLAY_PLACEHOLDER;
import static com.epherical.chatmanager.ChatManager.PLAYER_PLACEHOLDER;

@EventBusSubscriber(modid = ChatManager.MODID, bus = EventBusSubscriber.Bus.MOD, value = {Dist.DEDICATED_SERVER})
public class ServerEvents {

    private static final Logger LOGGER = LogUtils.getLogger();

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        if (ModList.get().isLoaded("luckperms")) {
            LuckPerms luckPermsApi = LuckPermsProvider.get();
            LuckPermsPlaceholders luckPermsPlaceholders = new LuckPermsPlaceholders(luckPermsApi);
            PlaceholderManager.register(DISPLAY_PLACEHOLDER, player -> ChatConfig.displayNameFormat);
            PlaceholderManager.register(PLAYER_PLACEHOLDER, player -> player.getName().getString());
            LOGGER.info("LuckPerms placeholders initialized");
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public void playersChatting(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();

        Channel channel = ChatManager.mod.getChannelManager().getChannel(player);

        channel.parseMessage(player, player.getUUID(), event.getUsername(), event.getRawText());
        event.setCanceled(true);
    }


    @SubscribeEvent
    public void onServerChat(MessagedParsedEvent event) {
        MinecraftServer server = event.getPlayer().getServer();

        server.getPlayerList().broadcastSystemMessage(event.getMessage(), player -> {
            MessageSendEvent post = NeoForge.EVENT_BUS.post(new MessageSendEvent(event.getPlayer(), player, event.getMessage()));
            if (post.isCanceled()) {
                return null; // Other listeners will handle if the event should send the message to the player or not.
            }
            return event.getMessage();
        }, false);
    }
}
