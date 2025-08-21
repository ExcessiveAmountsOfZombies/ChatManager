package com.epherical.chatmanager.config;

import com.epherical.chatmanager.chat.Channel;
import com.epherical.chatmanager.chat.ChannelType;
import com.epherical.epherolib.config.CommonConfig;
import com.epherical.epherolib.libs.org.spongepowered.configurate.CommentedConfigurationNode;
import com.epherical.epherolib.libs.org.spongepowered.configurate.loader.AbstractConfigurationLoader;
import com.epherical.epherolib.libs.org.spongepowered.configurate.serialize.SerializationException;
import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ChatManagerConfig extends CommonConfig {

    public String displayNameFormat = "<comp color='#d4af37'>[</comp><comp color='#ffffff'>{chatmanager:player}</comp><comp color='#d4af37'>]</comp>:";
    public Map<String,Channel> channels = new HashMap<>();

    public ChatManagerConfig(AbstractConfigurationLoader.Builder<?, ?> loaderBuilder, String configName) {
        super(loaderBuilder, configName);
    }

    @Override
    public void parseConfig(CommentedConfigurationNode root) {
        configVersion = root.node("version").getInt(configVersion);
        displayNameFormat = root.node("displayNameFormat").getString(displayNameFormat);

        channels.clear();

        try {
            List<Channel> channels1 = root.node("channels").getList(Channel.class, List.of());
            for (Channel channel : channels1) {
                channels.put(channel.name().toLowerCase(), channel);
            }
        } catch (SerializationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CommentedConfigurationNode generateConfig(CommentedConfigurationNode root) {

        try {
            root.node("version").set(configVersion).comment("Config Version, don't edit");
            root.node("display", "format")
                    .comment("Format for player display names. Supports placeholders such as "
                            + "{chatmanager:player}, {luckperms:prefix}, etc.")
                    .set("<comp color='#d4af37'>[</comp><comp color='#ffffff'>{chatmanager:player}</comp>"
                            + "<comp color='#d4af37'>]</comp>:");

            CommentedConfigurationNode channelsNode = root.node("channels");

            List<Channel> defaults = List.of(
                    new Channel(
                            "General",
                            List.of("general"),
                            "chatmanager.channel.general",
                            ChannelType.LOCAL,
                            "<comp color='#808080'>{message}</comp>"
                    ),
                    new Channel(
                            "Markets",
                            List.of("markets", "market"),
                            "chatmanager.channel.markets",
                            ChannelType.LOCAL,
                            "<comp color='#1abc9c'>{message}</comp>"
                    ),
                    new Channel(
                            "Offtopic",
                            List.of("offtopic", "off"),
                            "chatmanager.channel.offtopic",
                            ChannelType.LOCAL,
                            "<comp color='#b490ff'>{message}</comp>"
                    ),
                    new Channel(
                            "Help",
                            List.of("help"),
                            "chatmanager.channel.help",
                            ChannelType.LOCAL,
                            "<comp color='#ffbc40'>{message}</comp>"
                    )
            );

            channelsNode.setList(Channel.class, defaults);
        } catch (SerializationException e) {
            throw new RuntimeException(e);
        }
        return root;
    }

    @Override
    public Path getConfigPath(String modID) {
        return FMLPaths.CONFIGDIR.get().resolve(modID);
    }
}
