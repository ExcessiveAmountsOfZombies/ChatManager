package com.epherical.chatmanager.placeholders.register;


import com.epherical.chatmanager.ChatManager;
import com.epherical.chatmanager.placeholders.PHValue;
import com.epherical.chatmanager.placeholders.PlaceHolderManager;
import com.epherical.chatmanager.util.PlaceHolderContext;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

import static com.epherical.chatmanager.placeholders.PHValue.comp;
import static com.epherical.chatmanager.placeholders.PHValue.text;

public class PlayerPlaceholders extends AbstractPlaceholderCreator {

    private static final String ID = "player";

    public PlayerPlaceholders() {
        super(ID);
    }


    @Override
    protected void registerDefaults() {
        PlaceHolderManager.register(id("helmet_name"), context -> {
            ServerPlayer player = context.getPlayer();
            if (player != null) {
                if (!player.getInventory().getArmor(EquipmentSlot.HEAD.getIndex()).isEmpty()) {
                    ItemStack stack = player.getInventory().getArmor(EquipmentSlot.HEAD.getIndex());
                    return stack.getDisplayName().getString();
                }
                return "";
            }
            return "";
        });
        //PlaceHolderManager.register(id("player_name_component"), (ctx, params) -> playerDisplayName(ctx, params));

    }


    private static PHValue playerDisplayName(PlaceHolderContext ctx, String... ignored) {
        ServerPlayer player = ctx.getPlayer();
        if (player == null) {
            // Console / non-player context
            return text("Console");
        }

        // getDisplayName() already contains team colours, prefixes, etc.
        MutableComponent display = player.getDisplayName().copy();
        return comp(display);
    }


}
