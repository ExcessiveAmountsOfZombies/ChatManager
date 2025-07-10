package com.epherical.chatmanager.placeholders.register;


import com.epherical.chatmanager.placeholders.PlaceHolderManager;
import com.epherical.chatmanager.util.PlaceHolderContext;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

//{player_armor:defense,SLOT} -- Shows defense amount of the armor in the given slot.
//{player_armor:color,COLOR,SLOT} -- For dyed armor, gets the color component (CHOICES: red, green, blue, hex) in the slot.
//{player_armor:durability,MODE,SLOT} -- Durability info: current or max for the slot. (choices are: "current" or "max")
//{player_armor:material,SLOT} -- Name of the armor material in the slot.
public class PlayerPlaceholders extends AbstractPlaceholderCreator {

    private static final String ID = "player_armor";

    public PlayerPlaceholders() {
        super(ID);
    }

    @Override
    protected void registerDefaults() {
        // armor:amount,SLOT
        PlaceHolderManager.registerString(id("defense"), (ctx, params) -> {
            if (params.length == 0) return "";
            EquipmentSlot slot = parseSlot(params[0]);
            ItemStack stack = getArmor(ctx, slot);
            if (stack != null && stack.getItem() instanceof ArmorItem armor) {
                return String.valueOf(armor.getDefense());
            }
            return "0";
        });

        // armor:color,COLOR,SLOT
        PlaceHolderManager.registerString(id("color"), (ctx, params) -> {
            if (params.length < 2) return "";
            String color = params[0];
            EquipmentSlot slot = parseSlot(params[1]);
            ItemStack stack = getArmor(ctx, slot);

            boolean has = stack.has(DataComponents.DYED_COLOR);
            if (has) {
                int rgb = stack.get(DataComponents.DYED_COLOR).rgb();

                return switch (color.toLowerCase()) {
                    case "red" -> String.valueOf((rgb >> 16) & 0xFF);
                    case "green" -> String.valueOf((rgb >> 8) & 0xFF);
                    case "blue" -> String.valueOf(rgb & 0xFF);
                    case "hex" -> String.format("#%06X", rgb);
                    default -> "";
                };
            }

            return "";
        });


        // armor:durability,MODE,SLOT
        PlaceHolderManager.registerString(id("durability"), (ctx, params) -> {
            if (params.length < 2) return "";
            String mode = params[0];
            EquipmentSlot slot = parseSlot(params[1]);
            ItemStack stack = getArmor(ctx, slot);
            if (stack != null && stack.getMaxDamage() > 0) {
                return switch (mode.toLowerCase()) {
                    case "current" -> String.valueOf(stack.getMaxDamage() - stack.getDamageValue());
                    case "max" -> String.valueOf(stack.getMaxDamage());
                    default -> "";
                };
            }
            return "";
        });

        // armor:material,SLOT
        PlaceHolderManager.registerString(id("material"), (ctx, params) -> {
            if (params.length == 0) return "";
            EquipmentSlot slot = parseSlot(params[0]);
            ItemStack stack = getArmor(ctx, slot);
            if (stack != null && stack.getItem() instanceof ArmorItem armor) {
                String material = armor.getMaterial().getKey().location().toString().toLowerCase();
                if (material.contains(":")) {
                    material = material.substring(material.indexOf(':') + 1);
                }
                return material;
            }
            return "";
        });

    }

    private static EquipmentSlot parseSlot(String slot) {
        try {
            return EquipmentSlot.valueOf(slot.toUpperCase());
        } catch (Exception ignored) {
            return EquipmentSlot.HEAD;
        }
    }

    private static ItemStack getArmor(PlaceHolderContext ctx, EquipmentSlot slot) {
        if (ctx.getPlayer() instanceof ServerPlayer player) {
            return player.getItemBySlot(slot);
        }
        return ItemStack.EMPTY;
    }


}
