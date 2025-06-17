package com.epherical.chatmanager.client.events;


import com.epherical.chatmanager.ChatManager;
import net.minecraft.network.chat.ChatType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientChatEvent;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

@EventBusSubscriber(modid = ChatManager.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientInitializer {



    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        NeoForge.EVENT_BUS.register(new ScreenListener());
    }


    @SubscribeEvent
    public static void onClientReceivePlayerChat(ClientChatReceivedEvent.Player event) {
        ChatType.CHAT
    }


    @SubscribeEvent
    public static void onClientReceiveSystemChat(ClientChatReceivedEvent.System event) {


    }

}
