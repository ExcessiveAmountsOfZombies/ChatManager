package com.epherical.chatmanager.placeholders;


import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

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
    }

}
