package com.epherical.chatmanager.placeholders;

import com.epherical.chatmanager.util.PlaceHolderContext;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public final class LuckPermsPlaceholders {

    private final LuckPerms luckPerms;

    public LuckPermsPlaceholders(LuckPerms api) {
        this.luckPerms = api;
        registerDefaults();
    }

    private void registerDefaults() {
        PlaceHolderManager.register(
                ResourceLocation.fromNamespaceAndPath("luckperms", "prefix"),
                this::getPrefix
        );
        PlaceHolderManager.register(
                ResourceLocation.fromNamespaceAndPath("luckperms", "suffix"),
                this::getSuffix
        );
    }

    private String getPrefix(PlaceHolderContext player) {
        if (player == null) return "";
        User user = luckPerms.getUserManager().getUser(player.getPlayer().getUUID());
        if (user == null) return "";
        CachedMetaData meta = user.getCachedData().getMetaData();
        return meta.getPrefix() == null ? "" : meta.getPrefix();
    }

    private String getSuffix(PlaceHolderContext player) {
        if (player == null) return "";
        User user = luckPerms.getUserManager().getUser(player.getPlayer().getUUID());
        if (user == null) return "";
        CachedMetaData meta = user.getCachedData().getMetaData();
        return meta.getSuffix() == null ? "" : meta.getSuffix();
    }
}
