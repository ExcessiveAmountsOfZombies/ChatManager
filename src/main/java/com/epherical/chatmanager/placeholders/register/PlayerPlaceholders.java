package com.epherical.chatmanager.placeholders.register;


import com.epherical.chatmanager.placeholders.PlaceHolderManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class PlayerPlaceholders extends AbstractPlaceholderCreator {

    private static final String ID = "player";

    public PlayerPlaceholders() {
        super(ID);
    }


    @Override
    protected void registerDefaults() {
        PlaceHolderManager.registerString(id("helmet_name"), (ctx, par) -> {
            ServerPlayer player = ctx.getPlayer();
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


}
