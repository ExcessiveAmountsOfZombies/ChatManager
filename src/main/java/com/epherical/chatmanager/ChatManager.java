package com.epherical.chatmanager;

import com.epherical.chatmanager.chat.ChannelManager;
import com.epherical.chatmanager.commands.chat.DynamicChannelCommand;
import com.epherical.chatmanager.config.ChatConfig;
import com.epherical.chatmanager.util.ChatTypeVirtualPackResources;
import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.Optional;

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
        modEventBus.addListener(this::onAddPackFinders);
        NeoForge.EVENT_BUS.register(ServerEvents.class);
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

    private static final String PACK_ID = ChatManager.MODID + "_chat_types";


    public void onAddPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.SERVER_DATA) return;

        PackLocationInfo location = new PackLocationInfo(
                PACK_ID,
                Component.literal("ChatManager: Config-based Chat Types"),
                PackSource.SERVER,
                Optional.empty()
        );

        Pack.Metadata meta = new Pack.Metadata(
                Component.literal("Chat types generated from ChatManager config"),
                PackCompatibility.COMPATIBLE,
                net.minecraft.world.flag.FeatureFlagSet.of(),
                java.util.List.of(),
                true /* hidden → player cannot disable it */
        );

        Pack virtualPack = getPack(location, meta);

        // Expose it to the repository; it will be auto-enabled because we marked it so.
        event.addRepositorySource(list -> list.accept(virtualPack));
    }

    private static @NotNull Pack getPack(PackLocationInfo location, Pack.Metadata meta) {
        PackSelectionConfig config = new PackSelectionConfig(true, Pack.Position.TOP, true);

        return new Pack(
                location,
                new Pack.ResourcesSupplier() {

                    private final ChatTypeVirtualPackResources resources = new ChatTypeVirtualPackResources(location);

                    @Override
                    public PackResources openPrimary(PackLocationInfo location1) {
                        return resources;
                    }

                    @Override
                    public PackResources openFull(PackLocationInfo location1, Pack.Metadata metadata) {
                        return resources;
                    }
                },
                meta,
                config
        );
    }


    public ChannelManager getChannelManager() {
        return channelManager;
    }
}
