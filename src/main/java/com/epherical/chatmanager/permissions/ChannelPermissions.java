package com.epherical.chatmanager.permissions;

import com.epherical.chatmanager.ChatManager;
import com.epherical.chatmanager.chat.Channel;
import com.epherical.chatmanager.config.ChatConfig;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.server.permission.events.PermissionGatherEvent;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;
import net.neoforged.neoforge.server.permission.nodes.PermissionTypes;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps channel names to PermissionNodes and registers them with NeoForge's Permission system.
 */
public class ChannelPermissions {

    // channel_name_lowercase → PermissionNode<Boolean>
    public static final Map<String, PermissionNode<Boolean>> CHANNEL_PERMISSION_NODES = new HashMap<>();

    @SubscribeEvent
    public void onPermissionNodes(PermissionGatherEvent.Nodes event) {
        // For each channel declared in parsedChannels (should be ready at this phase)
        for (Channel channel : ChatConfig.parsedChannels.values()) {
            String perm = channel.permission();
            if (perm != null && !perm.isBlank()) {
                PermissionNode<Boolean> node = new PermissionNode<Boolean>(
                        ResourceLocation.fromNamespaceAndPath(ChatManager.MODID, perm),
                        PermissionTypes.BOOLEAN,
                        (player, stack, ctx) -> false // default: no access
                );
                CHANNEL_PERMISSION_NODES.put(channel.name().toLowerCase(), node);
                event.addNodes(node);
            }
        }
    }

    /**
     * Retrieve the PermissionNode for a given channel (by name, case-insensitive).
     * Returns null if the channel doesn't require a permission.
     */
    public static PermissionNode<Boolean> getPermissionNode(String channelName) {
        return CHANNEL_PERMISSION_NODES.get(channelName.toLowerCase());
    }
}
