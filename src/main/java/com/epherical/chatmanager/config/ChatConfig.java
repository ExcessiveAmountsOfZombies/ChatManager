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
                    .define("display.format", "<comp color='#d4af37'>[</comp><comp color='#ffffff'>{player}</comp><comp color='#d4af37'>]</comp>");

    // --- Chat channels ---
    public static final List<ChannelConfig> CHANNELS = new ArrayList<>();
    private static final List<ChannelConfigInternal> CHANNEL_CONFIGS = new ArrayList<>();

    static {
        CHANNEL_CONFIGS.add(new ChannelConfigInternal(
                "General",
                List.of("general"),
                "chatmanager.channel.general",
                "LOCAL",
                "{chatmanager:display}: <comp color='#808080'>{message}</comp>"
        ));
        CHANNEL_CONFIGS.add(new ChannelConfigInternal(
                "Markets",
                List.of("markets", "market"),
                "chatmanager.channel.markets",
                "LOCAL",
                "{chatmanager:display}: <comp color='#1abc9c'>{message}</comp>"
        ));
        CHANNEL_CONFIGS.add(new ChannelConfigInternal(
                "Offtopic",
                List.of("offtopic", "off"),
                "chatmanager.channel.offtopic",
                "LOCAL",
                "{chatmanager:display}: <comp color='#b490ff'>{message}</comp>"
        ));
        CHANNEL_CONFIGS.add(new ChannelConfigInternal(
                "Help",
                List.of("help"),
                "chatmanager.channel.help",
                "LOCAL",
                "{chatmanager:display}: <comp color='#ffbc40'>{message}</comp>"
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
