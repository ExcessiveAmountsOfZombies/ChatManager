package com.epherical.chatmanager.client.events;

import net.minecraft.network.chat.ChatType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;

public class ChatListener {



    @SubscribeEvent
    public void onClientReceivePlayerChat(ClientChatReceivedEvent.Player event) {
        ChatType.Bound boundChatType = event.getBoundChatType();
        System.out.println(boundChatType);

    }


    @SubscribeEvent
    public void onClientReceiveSystemChat(ClientChatReceivedEvent.System event) {


    }
}
