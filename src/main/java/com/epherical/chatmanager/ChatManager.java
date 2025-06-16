package com.epherical.chatmanager;

import com.epherical.chatmanager.chat.Channel;
import com.epherical.chatmanager.chat.ChannelManager;
import com.epherical.chatmanager.commands.chat.DynamicChannelCommand;
import com.epherical.chatmanager.config.ChatConfig;
import com.epherical.chatmanager.event.MessageSendEvent;
import com.epherical.chatmanager.event.MessagedParsedEvent;
import com.epherical.chatmanager.placeholders.LuckPermsPlaceholders;
import com.epherical.chatmanager.placeholders.PlaceholderManager;
import com.mojang.logging.LogUtils;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.EventBus;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;

@Mod(ChatManager.MODID)
public class ChatManager {
    public static final String MODID = "chatmanager";
    private static final Logger LOGGER = LogUtils.getLogger();

    // Chat components
    private ChannelManager channelManager;


    public static final ResourceLocation DISPLAY_PLACEHOLDER = ResourceLocation.fromNamespaceAndPath(MODID, "display");
    public static final ResourceLocation PLAYER_PLACEHOLDER = ResourceLocation.fromNamespaceAndPath(MODID, "player");

    public ChatManager(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        NeoForge.EVENT_BUS.register(this);
        modContainer.registerConfig(ModConfig.Type.COMMON, ChatConfig.SPEC, "chatmanager");
    }



    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            try {
                channelManager = new ChannelManager(this);
                LOGGER.info("Channel manager initialized");
            } catch (Exception e) {
                LOGGER.error("Error initializing chat components", e);
            }
        });
    }


    @SubscribeEvent
    public void onCommands(RegisterCommandsEvent event) {
        new DynamicChannelCommand(channelManager).register(event.getDispatcher());
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LuckPerms luckPermsApi = LuckPermsProvider.get();
        LuckPermsPlaceholders luckPermsPlaceholders = new LuckPermsPlaceholders(luckPermsApi);
        PlaceholderManager.register(DISPLAY_PLACEHOLDER, player -> ChatConfig.displayNameFormat);
        PlaceholderManager.register(PLAYER_PLACEHOLDER, player -> player.getName().getString());
        LOGGER.info("LuckPerms placeholders initialized");
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public void playersChatting(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();

        Channel channel = channelManager.getChannel(player);

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
