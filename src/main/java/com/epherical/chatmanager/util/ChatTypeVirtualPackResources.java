package com.epherical.chatmanager.util;

import com.epherical.chatmanager.ChatManager;
import com.epherical.chatmanager.chat.Channel;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.serialization.JsonOps;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.ChatTypeDecoration;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.resources.IoSupplier;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ChatTypeVirtualPackResources implements PackResources {
    // Map of all chat_type resources this pack has, keyed by ResourceLocation
    private final Map<ResourceLocation, IoSupplier<InputStream>> dataResources = new HashMap<>();

    private static final String NAMESPACE = ChatManager.MODID;
    private static final String PACK_META = """
            {
              "pack": {
                "pack_format": 25,
                "description": "ChatManager – config driven chat types"
              }
            }
            """;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final PackLocationInfo info;


    public ChatTypeVirtualPackResources(PackLocationInfo location) {
        buildResources();
        this.info = location;
    }

    /* --------------------------------------------------------------------- */
    /*  PackResources                                                        */
    /* --------------------------------------------------------------------- */

    @Override
    public void close() {
        // nothing to release
    }

    @Override
    public Set<String> getNamespaces(PackType type) {
        return type == PackType.SERVER_DATA ? Set.of(NAMESPACE) : Set.of();
    }

    @Nullable
    @Override
    public <T> T getMetadataSection(MetadataSectionSerializer<T> deserializer) throws IOException {
        return null;
    }

    @Override
    public PackLocationInfo location() {
        return info;
    }


    @Nullable
    @Override
    public IoSupplier<InputStream> getRootResource(String... path) {
        // Only provide pack.mcmeta
        if (path.length == 1 && path[0].equals("pack.mcmeta")) {
            return () -> new ByteArrayInputStream(PACK_META.getBytes(StandardCharsets.UTF_8));
        }
        return null;
    }

    // Provide a single resource for a chat type
    @Override
    public IoSupplier<InputStream> getResource(PackType type, ResourceLocation location) {
        return type == PackType.SERVER_DATA ? dataResources.get(location) : null;
    }

    // Allows the loader to find all chat type resources (e.g., for dynamic registration)
    @Override
    public void listResources(PackType type, String namespace, String prefix, ResourceOutput output) {
        if (type == PackType.SERVER_DATA) {
            for (Map.Entry<ResourceLocation, IoSupplier<InputStream>> entry : dataResources.entrySet()) {
                ResourceLocation location = entry.getKey();
                // Only include resources for the queried namespace and prefix (e.g., "chat_type/")
                if (location.getNamespace().equals(namespace) && location.getPath().startsWith(prefix)) {
                    output.accept(location, entry.getValue());
                }
            }
        }
    }

    private void buildResources() {
        dataResources.clear();

        for (Channel ch : ChatManager.mod.config.channels.values()) {
            String id = ch.name().toLowerCase().replaceAll(" ", "_");
            ResourceLocation fileLoc = ResourceLocation.fromNamespaceAndPath(
                    NAMESPACE, "chat_type/" + id + ".json"
            );

            String json = buildChatTypeJson(id);
            dataResources.put(fileLoc,
                    () -> new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)));
        }
    }

    /**
     * Builds a chat_type JSON using Minecraft's ChatType.CODEC, for best compatibility.
     */
    private static String buildChatTypeJson(String name) {
        ChatType chatType = new ChatType(
                ChatTypeDecoration.withSender("chat.chatmanager.chat"),
                new ChatTypeDecoration("chat.chatmanager.chat", List.of(ChatTypeDecoration.Parameter.CONTENT), Style.EMPTY)
        );
        var result = ChatType.DIRECT_CODEC.encodeStart(JsonOps.INSTANCE, chatType);
        if (result.result().isPresent()) {
            return GSON.toJson(result.result().get());
        } else {
            throw new IllegalStateException(
                    "Failed to serialize ChatType for: " + name + " - " +
                            result.error().map(e -> e.message()).orElse("Unknown error")
            );
        }
    }

}
