package com.epherical.chatmanager.commands.chat;

import com.epherical.chatmanager.chat.Channel;
import com.epherical.chatmanager.chat.ChannelManager;
import com.epherical.chatmanager.config.ChatConfig;
import com.epherical.chatmanager.permissions.ChannelPermissions;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.server.permission.PermissionAPI;

public class DynamicChannelCommand {

    private final ChannelManager channelManager;

    public DynamicChannelCommand(ChannelManager manager) {
        this.channelManager = manager;
    }

    // Suggest channel names and aliases for tab completion
    private static final SuggestionProvider<CommandSourceStack> CHANNEL_SUGGESTIONS = (ctx, builder) -> {
        for (Channel chan : ChatConfig.parsedChannels.values()) {
            builder.suggest(chan.name());
            for (String alias : chan.aliases()) {
                builder.suggest(alias);
            }
        }
        return builder.buildFuture();
    };

    // Helper: lookup Channel by name/alias (case insensitive)
    private static Channel getChannelByNameOrAlias(String input) {
        String normalized = input.toLowerCase();
        Channel match = ChatConfig.parsedChannels.get(normalized);
        if (match != null) {
            return match;
        }
        // Check aliases
        for (Channel chan : ChatConfig.parsedChannels.values()) {
            for (String alias : chan.aliases()) {
                if (alias.equalsIgnoreCase(input)) return chan;
            }
        }
        return null;
    }

    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("join")
                .then(Commands.argument("channel", StringArgumentType.word())
                        .suggests(CHANNEL_SUGGESTIONS)
                        .executes(ctx -> {
                            String input = StringArgumentType.getString(ctx, "channel");
                            Channel chan = getChannelByNameOrAlias(input);
                            ServerPlayer player = ctx.getSource().getPlayerOrException();

                            if (chan == null) {
                                player.sendSystemMessage(Component.literal("Unknown channel: " + input));
                                return 0;
                            }

                            // Permissions
                            var permissionNode = ChannelPermissions.getPermissionNode(chan.name());
                            if (permissionNode != null && !PermissionAPI.getPermission(player, permissionNode)) {
                                player.sendSystemMessage(Component.literal("You do not have permission to join channel: " + chan.name()));
                                return 0;
                            }

                            channelManager.unmuteChannel(player, chan.name());
                            channelManager.joinChannel(player, chan.name());
                            player.sendSystemMessage(Component.literal("You have joined channel: " + chan.name()));
                            return 1;
                        })
                )
        );

        dispatcher.register(Commands.literal("leave")
                .then(Commands.argument("channel", StringArgumentType.word())
                        .suggests(CHANNEL_SUGGESTIONS)
                        .executes(ctx -> {
                            String input = StringArgumentType.getString(ctx, "channel");
                            Channel chan = getChannelByNameOrAlias(input);
                            ServerPlayer player = ctx.getSource().getPlayerOrException();

                            if (chan == null) {
                                player.sendSystemMessage(Component.literal("Unknown channel: " + input));
                                return 0;
                            }

                            if (channelManager.getCurrentChannel(player) != null &&
                                    channelManager.getCurrentChannel(player).equalsIgnoreCase(chan.name())) {
                                player.sendSystemMessage(Component.literal("You cannot leave the currently active channel; switch first."));
                                return 0;
                            }
                            channelManager.leaveChannel(player, chan.name());
                            player.sendSystemMessage(Component.literal("You have muted channel: " + chan.name()));
                            return 1;
                        })
                )
        );
    }
}
