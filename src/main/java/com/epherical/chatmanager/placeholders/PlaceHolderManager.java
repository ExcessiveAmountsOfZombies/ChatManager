package com.epherical.chatmanager.placeholders;

import com.epherical.chatmanager.util.PlaceHolderContext;
import com.epherical.chatmanager.util.ComponentParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * A placeholder may be registered either as a plain-text supplier or as a
 * component supplier.  Only one internal registry is used.
 */
public final class PlaceHolderManager {

    /* --------------------------------------------------------------------- */
    /* Public supplier types                                                  */
    /* --------------------------------------------------------------------- */

    /** Produces a component directly. */
    @FunctionalInterface
    public interface ComponentSupplier {
        MutableComponent apply(PlaceHolderContext ctx, String... params);
    }

    /** Produces a String; we will wrap it in Component.literal for you. */
    @FunctionalInterface
    public interface StringSupplier {
        String apply(PlaceHolderContext ctx, String... params);
    }

    /* --------------------------------------------------------------------- */
    /* Private wrapper used in the single registry                           */
    /* --------------------------------------------------------------------- */

    private sealed interface Replacement
            permits Replacement.Text, Replacement.Comp {

        MutableComponent resolve(PlaceHolderContext ctx, String[] params);

        /* ---------- implementations ---------- */

        record Text(StringSupplier supplier) implements Replacement {
            @Override
            public MutableComponent resolve(PlaceHolderContext ctx, String[] params) {
                return Component.literal(supplier.apply(ctx, params));
            }
        }

        record Comp(ComponentSupplier supplier) implements Replacement {
            @Override
            public MutableComponent resolve(PlaceHolderContext ctx, String[] params) {
                return supplier.apply(ctx, params);
            }
        }
    }

    /* --------------------------------------------------------------------- */
    /* Registry & pattern                                                     */
    /* --------------------------------------------------------------------- */

    private static final Map<ResourceLocation, Replacement> REGISTRY = new HashMap<>();
    /*
     *  {my_mod:placeholder,param1,param2,...}
     *
     *  group(1) ->  my_mod:placeholder
     *  group(2) ->  param1,param2,...   (may be null / empty if no params)
     */
    private static final Pattern PLACEHOLDER_PATTERN =
            Pattern.compile("\\{([a-z0-9_.:-]+)(?:,([^{}]*))?}", Pattern.CASE_INSENSITIVE);

    private PlaceHolderManager() {}

    /* ----------------------------------------------------------------- */
    /* Public registration helpers                                       */
    /* ----------------------------------------------------------------- */

    /** Register a placeholder that returns a **component**. */
    public static void registerComponent(ResourceLocation id, ComponentSupplier supplier) {
        REGISTRY.put(id, new Replacement.Comp(supplier));
    }

    /** Register a placeholder that returns **plain text**. */
    public static void registerString(ResourceLocation id, StringSupplier supplier) {
        REGISTRY.put(id, new Replacement.Text(supplier));
    }

    /* --------------------------------------------------------------------- */
    /* Expansion                                                              */
    /* --------------------------------------------------------------------- */

    public static MutableComponent process(String raw, PlaceHolderContext ctx) {
        MutableComponent out = Component.empty();
        Matcher matcher      = PLACEHOLDER_PATTERN.matcher(raw);

        int lastEnd = 0;
        while (matcher.find()) {
            // prefix between previous and current token
            if (matcher.start() > lastEnd) {
                out.append(ComponentParser.parse(raw.substring(lastEnd, matcher.start())));
            }

            /* -------- resolve the found placeholder --------------------- */

            ResourceLocation id = ResourceLocation.parse(matcher.group(1));
            String   rawParamList = matcher.group(2);         // may be null
            String[] params       = (rawParamList == null || rawParamList.isEmpty())
                                    ? new String[0]
                                    : rawParamList.split(",", -1);

            Replacement repl = REGISTRY.get(id);
            if (repl != null) {
                out.append(repl.resolve(ctx, params));
            } else {                               // unknown → keep visible
                out.append(Component.literal(matcher.group()));
            }

            lastEnd = matcher.end();
        }

        // trailing suffix
        if (lastEnd < raw.length()) {
            out.append(ComponentParser.parse(raw.substring(lastEnd)));
        }

        return out;
    }
}
