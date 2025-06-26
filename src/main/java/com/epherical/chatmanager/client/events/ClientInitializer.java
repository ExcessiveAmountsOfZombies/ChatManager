package com.epherical.chatmanager.client.events;


import com.epherical.chatmanager.ChatManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.common.NeoForge;

@EventBusSubscriber(modid = ChatManager.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientInitializer {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        NeoForge.EVENT_BUS.register(new ScreenListener());
        NeoForge.EVENT_BUS.register(new ChatListener());
    }

}
