package com.epherical.chatmanager.mixin.client;

import com.epherical.chatmanager.client.events.ChatListener;
import com.epherical.chatmanager.compat.ChatHeadsReflector;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(value = ChatComponent.class, priority = 980)
public class ChatComponentMixin {


    // Intercept the addMessage(Component, MessageSignature, GuiMessageTag)
    @Inject(
            method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V",
            at = @At("HEAD")
    )
    private void onAddMessageFull(Component chatComponent, @Nullable MessageSignature signature, @Nullable GuiMessageTag tag, CallbackInfo ci) {
        forwardToBase(chatComponent, signature, tag);
    }

    private void forwardToBase(Component chatComponent, @Nullable MessageSignature signature, @Nullable GuiMessageTag tag) {
        Minecraft minecraft = Minecraft.getInstance();
        Object lastSenderData = ChatHeadsReflector.getLastSenderData();
        if (minecraft != null && minecraft.gui != null) {
            // Don't reforward if we're already on the vanilla chat component—that would double-print!
            ChatComponent thisComponent = (ChatComponent) (Object) this;
            if (!thisComponent.equals(ChatListener.chatComponent)) {
                ChatListener.chatComponent.addMessage(chatComponent, signature, tag);
            }
        }
        ChatHeadsReflector.setLastSenderData(lastSenderData);

    }
}
