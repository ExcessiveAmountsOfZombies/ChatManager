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
import net.neoforged.fml.ModList;
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


    public static ChatManager mod;

    // Chat components
    private ChannelManager channelManager;


    public static final ResourceLocation DISPLAY_PLACEHOLDER = ResourceLocation.fromNamespaceAndPath(MODID, "display");
    public static final ResourceLocation PLAYER_PLACEHOLDER = ResourceLocation.fromNamespaceAndPath(MODID, "player");

    public ChatManager(IEventBus modEventBus, ModContainer modContainer) {
        mod = this;
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


    public ChannelManager getChannelManager() {
        return channelManager;
    }
}
