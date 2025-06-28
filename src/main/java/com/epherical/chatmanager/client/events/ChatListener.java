package com.epherical.chatmanager.client.events;

import com.epherical.chatmanager.ChatManager;
import com.epherical.chatmanager.client.ClientChannelManager;
import com.epherical.chatmanager.mixin.client.GuiAccessorMixin;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ChatType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

public class ChatListener {


    public static ClientChannelManager manager;


    @SubscribeEvent
    public void onClientReceivePlayerChat(ClientChatReceivedEvent.Player event) {
        // handle in mixin
    }


    @SubscribeEvent
    public void onClientReceiveSystemChat(ClientChatReceivedEvent.System event) {


    }


    @SubscribeEvent
    public void onClientJoin(ClientPlayerNetworkEvent.LoggingIn event) {
        manager = new ClientChannelManager();
        var level = event.getPlayer().registryAccess();
        // Access the ChatType registry
        var chatTypeRegistry = level.registryOrThrow(Registries.CHAT_TYPE);
        // Iterate and initialize your ClientChannelManager here

        for (ResourceKey<ChatType> chatType : chatTypeRegistry.registryKeySet()) {
            ResourceLocation id = chatType.location();
            if (id.getNamespace().equals(ChatManager.MODID)) {
                manager.addChannel(chatType);
            }
        }
    }
}
