package com.epherical.chatmanager.compat.placeholders;

import com.epherical.chatmanager.placeholders.PlaceHolderManager;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.ftb.mods.ftbranks.FTBRanks;
import dev.ftb.mods.ftbranks.api.FTBRanksAPI;
import dev.ftb.mods.ftbranks.api.Rank;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.Comparator;
import java.util.List;

public class FTBRanksPlaceholders {

    private static final String FTB_RANKS_MODID = "ftbranks";


    public FTBRanksPlaceholders() {
        registerDefaults();
    }


    private void registerDefaults() {
        PlaceHolderManager.register(id("rank"),
                context -> getRank(context.getPlayer()));
    }


    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(FTB_RANKS_MODID, path);
    }

    private static String getRank(ServerPlayer player) {
        if (player == null) {
            return "";
        }
        List<Rank> ranks = FTBRanksAPI.manager().getRanks(player).stream()
                .sorted(Comparator.comparing(Rank::getPower).reversed()).toList();

        if (ranks.isEmpty()) {
            return "";
        } else {
            return ranks.getFirst().getName();
        }

    }


}
