package com.epherical.chatmanager.client.events;

import com.epherical.chatmanager.ChatManager;
import com.epherical.chatmanager.client.ClientChannelManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ChatType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

public class ChatListener {


    public static ClientChannelManager manager;
    public static ChatComponent chatComponent;


    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onClientReceivePlayerChat(ClientChatReceivedEvent.Player event) {
        // handle in mixin
        Minecraft mc = Minecraft.getInstance();
        ChatComponent chat = mc.gui.getChat();
        if (chatComponent.equals(chat)) {
            ChatComponent chatComponent1 = manager.getChatComponent(event.getBoundChatType().chatType().getKey());
            chatComponent1.addMessage(event.getMessage(), null, null);
            event.setCanceled(true);
        }
    }


    @SubscribeEvent
    public void onClientReceiveSystemChat(ClientChatReceivedEvent.System event) {


    }


    @SubscribeEvent
    public void onClientJoin(ClientPlayerNetworkEvent.LoggingIn event) {
        manager = new ClientChannelManager();
        chatComponent = Minecraft.getInstance().gui.getChat();
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
