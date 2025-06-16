package com.epherical.chatmanager.chat;

import com.epherical.chatmanager.ChatManager;
import com.epherical.chatmanager.config.ChatConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

public class ChannelManager {
    private static final String CHANNEL_PREF_KEY = "redisglobalchat.channel.current";
    private static final String MUTE_PREFIX = "redisglobalchat.channel.mute.";

    /**
     * Join (switch to) a channel. Store their channel preference for future logins.
     */

    private final ChatManager mod;

    public ChannelManager(ChatManager mod) {
        this.mod = mod;
    }

    public Channel getChannel(ServerPlayer player) {
        String currentChannel = getCurrentChannel(player);
        return ChatConfig.parsedChannels.get(currentChannel.toLowerCase());
    }

    public Channel getChannelByName(String channelName) {
        return ChatConfig.parsedChannels.get(channelName.toLowerCase());
    }



    public void joinChannel(ServerPlayer player, String channelName) {
        CompoundTag data = player.getPersistentData();
        data.putString(CHANNEL_PREF_KEY, channelName);
    }

    /**
     * Get the player's current preferred channel.
     * Returns null if not set.
     */
    public String getCurrentChannel(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        return data.contains(CHANNEL_PREF_KEY) ? data.getString(CHANNEL_PREF_KEY) : null;
    }

    /**
     * Leave (mute) a channel. This marks the channel as muted for this player.
     */
    public void leaveChannel(ServerPlayer player, String channelName) {
        CompoundTag data = player.getPersistentData();
        data.putBoolean(MUTE_PREFIX + channelName, true);
    }

    /**
     * Check if player has muted (left) this channel.
     */
    public boolean isChannelMuted(ServerPlayer player, String channelName) {
        CompoundTag data = player.getPersistentData();
        return data.getBoolean(MUTE_PREFIX + channelName);
    }

    /**
     * Undo a mute (unmute a channel for this player).
     */
    public void unmuteChannel(ServerPlayer player, String channelName) {
        CompoundTag data = player.getPersistentData();
        data.remove(MUTE_PREFIX + channelName);
    }
}
