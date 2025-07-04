package com.epherical.chatmanager.listener;

import com.epherical.chatmanager.ChatManager;
import com.epherical.chatmanager.util.ChatMessenger;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = ChatManager.MODID, bus = EventBusSubscriber.Bus.GAME, value = {Dist.DEDICATED_SERVER})
public class NameFormatListener {


    @SubscribeEvent
    public static void onNameFormat(PlayerEvent.NameFormat event) {
        /*if (event.getEntity() instanceof LocalPlayer) {
            return;
        }
        event.setDisplayname(replaceName((ServerPlayer) event.getEntity(), event.getUsername().getString(), ChatConfig.displayNameFormat));*/
    }

    @SubscribeEvent
    public static void onTabListNameFormat(PlayerEvent.TabListNameFormat event) {
        /*if (event.getEntity() instanceof LocalPlayer) {
            return;
        }
        event.setDisplayName(replaceName((ServerPlayer) event.getEntity(), event.getEntity().getName().getString(), ChatConfig.displayNameFormat));*/
    }

    private static MutableComponent replaceName(ServerPlayer player, String name, String formatter) {
        return ChatMessenger.parse(player, formatter);
    }
}
