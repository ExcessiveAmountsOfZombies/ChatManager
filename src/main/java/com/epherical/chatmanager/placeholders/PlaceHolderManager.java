package com.epherical.chatmanager.placeholders;

import com.epherical.chatmanager.util.PlaceHolderContext;
import com.epherical.chatmanager.util.text.TemplateParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public final class PlaceHolderManager {

    @FunctionalInterface
    public interface ComponentSupplier {
        MutableComponent apply(PlaceHolderContext ctx, String... params);
    }

    @FunctionalInterface
    public interface StringSupplier {
        String apply(PlaceHolderContext ctx, String... params);
    }

    private sealed interface Replacement permits Replacement.Text, Replacement.Comp {

        MutableComponent resolve(PlaceHolderContext ctx, String[] params);


        record Comp(ComponentSupplier supplier) implements Replacement {
            @Override
            public MutableComponent resolve(PlaceHolderContext ctx, String[] params) {
                return supplier.apply(ctx, params);
            }
        }

        record Text(StringSupplier supplier) implements Replacement {
            @Override
            public MutableComponent resolve(PlaceHolderContext ctx, String[] params) {
                return Component.literal(supplier.apply(ctx, params));
            }
        }
    }

    private static final Map<ResourceLocation, Replacement> REGISTRY = new HashMap<>();

    private PlaceHolderManager() {
    }

    /**
     * Register a placeholder that returns a **component**.
     */
    public static void registerComponent(ResourceLocation id, ComponentSupplier supplier) {
        REGISTRY.put(id, new Replacement.Comp(supplier));
    }

    /**
     * Register a placeholder that returns **plain text**.
     */
    public static void registerString(ResourceLocation id, StringSupplier supplier) {
        REGISTRY.put(id, new Replacement.Text(supplier));
    }


    public static MutableComponent process(String raw, PlaceHolderContext ctx) {
        var template = TemplateParser.parse(raw);
        return template.emit(ctx);
    }


    public static MutableComponent resolve(ResourceLocation id, PlaceHolderContext ctx, String[] params) {
        Replacement rep = REGISTRY.get(id);
        if (rep == null) {
            return Component.literal("{" + id + (params.length > 0 ? "," + String.join(",", params) : "") + "}");
        }
        return rep.resolve(ctx, params);
    }
}
