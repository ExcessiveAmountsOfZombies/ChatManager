package com.epherical.chatmanager.mixin.client;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Gui.class)
public interface GuiAccessorMixin {
    @Accessor("chat")
    ChatComponent getGuiChatComponent();
}