package com.epherical.chatmanager.config;

import com.epherical.chatmanager.ChatManager;
import com.epherical.chatmanager.chat.Channel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EventBusSubscriber(modid = ChatManager.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ChatConfig {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // --- Display name formatting ---
    public static final ModConfigSpec.ConfigValue<String> DISPLAY_NAME_FORMAT =
            BUILDER.comment("Format for player display names. Supports placeholders such as {player}, {luckperms:prefix}, etc.")
                    .define("display.format", "<comp color='#d4af37'>[{luckperms:prefix}]</comp> <comp color='#43ff43'>{chatmanager:player}</comp>");

    // --- Chat channels ---
    public static final List<ChannelConfig> CHANNELS = new ArrayList<>();
    private static final List<ChannelConfigInternal> CHANNEL_CONFIGS = new ArrayList<>();

    static {
        // Example default channel
        CHANNEL_CONFIGS.add(new ChannelConfigInternal(
                "Global",
                List.of("g", "global"),
                "redisglobalchat.channel.global",
                "GLOBAL",
                "<comp color='#b2cefc'>[G|{redisglobalchat:server}]</comp> {display} &7: <comp color='#ff00ff'>{message}</comp>"
        ));

        CHANNEL_CONFIGS.add(new ChannelConfigInternal(
                "General",
                List.of("general"),
                "redisglobalchat.channel.general",
                "LOCAL",
                "<comp color='#bfedb2'></comp> {chatmanager:display} &7: <comp color='#ffffff'>{message}</comp>"
        ));

        for (ChannelConfigInternal internal : CHANNEL_CONFIGS) {
            String path = "channels." + internal.name;
            BUILDER.comment("Configuration for the " + internal.name + " chat channel.");
            ModConfigSpec.ConfigValue<List<? extends String>> aliases =
                    BUILDER.defineList(path + ".aliases", internal.aliases, o -> o instanceof String);
            ModConfigSpec.ConfigValue<String> permission =
                    BUILDER.define(path + ".permission", internal.permission);
            ModConfigSpec.ConfigValue<String> type =
                    BUILDER.define(path + ".type", internal.type);
            ModConfigSpec.ConfigValue<String> format =
                    BUILDER.define(path + ".format", internal.format);

            CHANNELS.add(new ChannelConfig(
                    internal.name, aliases, permission, type, format
            ));
        }
    }

    public static final ModConfigSpec SPEC = BUILDER.build();

    /**
     * Loaded and parsed channel configuration.
     */
    public record ChannelConfig(String name, ModConfigSpec.ConfigValue<List<? extends String>> aliases,
                                ModConfigSpec.ConfigValue<String> permission, ModConfigSpec.ConfigValue<String> type,
                                ModConfigSpec.ConfigValue<String> format) {
    }

    private record ChannelConfigInternal(String name, List<String> aliases, String permission, String type,
                                         String format) {
    }

    public static String displayNameFormat;
    public static final Map<String, Channel> parsedChannels = new HashMap<>();

    @SubscribeEvent
    public static void onLoad(final ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() != SPEC) return;

        displayNameFormat = DISPLAY_NAME_FORMAT.get();
        parsedChannels.clear();
        for (ChannelConfig config : CHANNELS) {
            parsedChannels.put(config.name.toLowerCase(), new Channel(
                    config.name,
                    new ArrayList<>(config.aliases.get()),
                    config.permission.get(),
                    config.type.get(),
                    config.format.get()
            ));
        }
    }

}
