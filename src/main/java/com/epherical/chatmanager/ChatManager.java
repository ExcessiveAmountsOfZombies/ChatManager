package com.epherical.chatmanager;

import com.epherical.chatmanager.chat.ChannelManager;
import com.epherical.chatmanager.commands.chat.DynamicChannelCommand;
import com.epherical.chatmanager.config.ChatConfig;
import com.epherical.chatmanager.listener.NameFormatListener;
import com.epherical.chatmanager.listener.ServerEvents;
import com.epherical.chatmanager.permissions.ChannelPermissions;
import com.epherical.chatmanager.placeholders.PlaceHolderManager;
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
    public static final ResourceLocation MSPT_PLACEHOLDER = ResourceLocation.fromNamespaceAndPath(MODID, "mspt");
    public static final ResourceLocation LEVEL_PLACEHOLDER = ResourceLocation.fromNamespaceAndPath(MODID, "level");
    public static final ResourceLocation MOTD_PLACEHOLDER = ResourceLocation.fromNamespaceAndPath(MODID, "motd");
    public static final ResourceLocation PLAYER_COUNT = ResourceLocation.fromNamespaceAndPath(MODID, "player_count");
    public static final ResourceLocation PLAYER_PING = ResourceLocation.fromNamespaceAndPath(MODID, "player_ping");
    public static final ResourceLocation PLAYER_X = ResourceLocation.fromNamespaceAndPath(MODID, "player_x");
    public static final ResourceLocation PLAYER_Y = ResourceLocation.fromNamespaceAndPath(MODID, "player_y");
    public static final ResourceLocation PLAYER_Z = ResourceLocation.fromNamespaceAndPath(MODID, "player_z");
    public static final ResourceLocation PLAYER_XYZ = ResourceLocation.fromNamespaceAndPath(MODID, "player_xyz");
    public static final ResourceLocation PLAYER_MAX_HEALTH = ResourceLocation.fromNamespaceAndPath(MODID, "player_max_health");
    public static final ResourceLocation PLAYER_HEALTH = ResourceLocation.fromNamespaceAndPath(MODID, "player_health");
    public static final ResourceLocation PLAYER_HUNGER = ResourceLocation.fromNamespaceAndPath(MODID, "player_hunger");
    public static final ResourceLocation WORLD_TIME = ResourceLocation.fromNamespaceAndPath(MODID, "world_time");
    public static final ResourceLocation WORLD_DAY = ResourceLocation.fromNamespaceAndPath(MODID, "world_day");


    public ChatManager(IEventBus modEventBus, ModContainer modContainer) {
        mod = this;
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::onAddPackFinders);
        NeoForge.EVENT_BUS.register(ServerEvents.class);
        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.register(new ChannelPermissions());
        NeoForge.EVENT_BUS.register(NameFormatListener.class);
        modContainer.registerConfig(ModConfig.Type.COMMON, ChatConfig.SPEC, "chatmanager");


        PlaceHolderManager.register(DISPLAY_PLACEHOLDER, player -> ChatConfig.displayNameFormat);
        PlaceHolderManager.register(PLAYER_PLACEHOLDER, player -> player.getPlayer().getDisplayName().getString());
        PlaceHolderManager.register(MSPT_PLACEHOLDER, ctx -> {
            if (ctx.getServer() != null) {
                double tps = ctx.getServer().tickRateManager().millisecondsPerTick();
                // If you have an array of TPS values, take the average or a specific value (e.g., last 1 minute)
                // double tps = ctx.getServer().recentTps()[0];
                return String.format("%.2f", tps);
            }
            return "?";
        });
        PlaceHolderManager.register(LEVEL_PLACEHOLDER, ctx -> {
            if (ctx.getLevel() != null) {
                return ctx.getLevel().dimension().location().getPath();
            }
            if (ctx.getPlayer() != null) {
                return ctx.getPlayer().level().dimension().location().getPath();
            }
            return "unknown";
        });
        PlaceHolderManager.register(MOTD_PLACEHOLDER, ctx -> {
            if (ctx.getServer() != null) {
                return ctx.getServer().getMotd();
            }
            return "Minecraft Server";
        });
        PlaceHolderManager.register(PLAYER_COUNT, ctx -> {
            if (ctx.getServer() != null && ctx.getServer().getPlayerList() != null) {
                return String.valueOf(ctx.getServer().getPlayerList().getPlayerCount());
            }
            return "?";
        });

        PlaceHolderManager.register(PLAYER_PING, ctx -> {
            if (ctx.getPlayer() != null) {
                return String.valueOf(ctx.getPlayer().connection.latency());
            }
            return "?";
        });

        PlaceHolderManager.register(PLAYER_X, ctx -> {
            if (ctx.getPlayer() != null) {
                return String.format("%.2f", ctx.getPlayer().getX());
            }
            return "?";
        });

        PlaceHolderManager.register(PLAYER_Y, ctx -> {
            if (ctx.getPlayer() != null) {
                return String.format("%.2f", ctx.getPlayer().getY());
            }
            return "?";
        });

        PlaceHolderManager.register(PLAYER_Z, ctx -> {
            if (ctx.getPlayer() != null) {
                return String.format("%.2f", ctx.getPlayer().getZ());
            }
            return "?";
        });

        PlaceHolderManager.register(PLAYER_XYZ, ctx -> {
            if (ctx.getPlayer() != null) {
                return String.format("%.2f, %.2f, %.2f", ctx.getPlayer().getX(), ctx.getPlayer().getY(), ctx.getPlayer().getZ());
            }
            return "?";
        });


        /*PlaceHolderManager.register(PLAYER_MAX_HEALTH, ctx -> {
            if (ctx.getPlayer() != null) {
                return String.format("%.0f", ctx.getPlayer().getMaxHealth());
            }
            return "?";
        });

        PlaceHolderManager.register(PLAYER_HEALTH, ctx -> {
            if (ctx.getPlayer() != null) {
                return String.format("%.0f", ctx.getPlayer().getHealth());
            }
            return "?";
        });*/

        PlaceHolderManager.register(PLAYER_HUNGER, ctx -> {
            if (ctx.getPlayer() != null) {
                return String.valueOf(ctx.getPlayer().getFoodData().getFoodLevel());
            }
            return "?";
        });

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
