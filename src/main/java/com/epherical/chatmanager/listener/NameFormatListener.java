package com.epherical.chatmanager.listener;

import com.epherical.chatmanager.config.ChatConfig;
import com.epherical.chatmanager.util.ChatMessenger;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;


public class NameFormatListener {


    @SubscribeEvent
    public void onNameFormat(PlayerEvent.NameFormat event) {
        event.setDisplayname(replaceName((ServerPlayer) event.getEntity(), event.getUsername().getString(), ChatConfig.displayNameFormat));
    }

    @SubscribeEvent
    public void onTabListNameFormat(PlayerEvent.TabListNameFormat event) {
        event.setDisplayName(replaceName((ServerPlayer) event.getEntity(), event.getEntity().getName().getString(), ChatConfig.displayNameFormat));
    }

    private MutableComponent replaceName(ServerPlayer player, String name, String formatter) {
        return ChatMessenger.parse(player, formatter);
    }
}
