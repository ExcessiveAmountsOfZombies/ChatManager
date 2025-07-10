package com.epherical.chatmanager.commands.chat;

import com.epherical.chatmanager.placeholders.PlaceHolderManager;
import com.epherical.chatmanager.util.ChatMessenger;
import com.mojang.brigadier.Command;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public class PlaceHolderTest {


    public static void registerTestCommand(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("testarmorplaceholders")
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();

                            // Example of evaluating several placeholders for all armor slots
                            String[] slots = { "HEAD", "CHEST", "LEGS", "FEET" };
                            StringBuilder message = new StringBuilder("Armor placeholders:\n");

                            for (String slot : slots) {
                                ChatMessenger.send(player, ("{player_armor:defense," + slot + "}"));
                                ChatMessenger.send(player, "{player_armor:material," + slot + "}");
                                ChatMessenger.send(player, "{player_armor:durability,current," + slot + "}");
                                ChatMessenger.send(player, "{player_armor:color,red," + slot + "} LOL COLOR");
                                //message.append("%s - Defense: %s, Material: %s, Durability: %s\n".formatted(slot, def, mat, dura));
                            }

                            player.sendSystemMessage(Component.literal(message.toString()));
                            return Command.SINGLE_SUCCESS;
                        })
        );
    }

}
