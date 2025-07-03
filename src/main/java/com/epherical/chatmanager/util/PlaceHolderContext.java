package com.epherical.chatmanager.util;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public class PlaceHolderContext {
    @Nullable private final MinecraftServer server;
    @Nullable private final ServerLevel level;
    @Nullable private final ServerPlayer player;

    public PlaceHolderContext(@Nullable MinecraftServer server, @Nullable ServerLevel level, @Nullable ServerPlayer player) {
        this.server = server;
        this.level = level;
        this.player = player;
    }

    public @Nullable MinecraftServer getServer() { return server; }
    public @Nullable ServerLevel getLevel() { return level; }
    public @Nullable ServerPlayer getPlayer() { return player; }


    public static PlaceHolderContext create(@Nullable MinecraftServer server, @Nullable ServerLevel level, @Nullable ServerPlayer player) {
        return new PlaceHolderContext(server, level, player);
    }

    public static PlaceHolderContext create(@Nullable MinecraftServer server, @Nullable ServerLevel level) {
        return new PlaceHolderContext(server, level, null);
    }

    public static PlaceHolderContext create(@Nullable MinecraftServer server) {
        return new PlaceHolderContext(server, null, null);
    }

    public static PlaceHolderContext create(ServerPlayer player) {
        return new PlaceHolderContext(null, null, player);
    }

    public static PlaceHolderContext create(ServerLevel level) {
        return new PlaceHolderContext(null, level, null);
    }

    public static PlaceHolderContext create(ServerLevel level, ServerPlayer player) {
        return new PlaceHolderContext(null, level, player);
    }
}
