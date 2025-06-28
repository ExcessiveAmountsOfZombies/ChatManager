package com.epherical.chatmanager.mixin.client;

import com.epherical.chatmanager.client.ClientChannelManager;
import com.epherical.chatmanager.client.events.ChatListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.ChatType;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Gui.class)
public class GuiMixin {

    @Inject(method = "getChat", at = @At("HEAD"), cancellable = true)
    private void interceptGetChat(CallbackInfoReturnable<ChatComponent> cir) {
        // Get the current channel from your manager
        ClientChannelManager manager = ChatListener.manager;
        ResourceKey<ChatType> selectedChannel = manager.getCurrentChannel(); // implement this accessor as needed

        if (selectedChannel != null) {
            ChatComponent channelChat = manager.getChatComponent(selectedChannel);
            if (channelChat != null) {
                cir.setReturnValue(channelChat);
            }
        }
        // Fallback case: return the default
        // Don't set, let vanilla method run
    }



}
