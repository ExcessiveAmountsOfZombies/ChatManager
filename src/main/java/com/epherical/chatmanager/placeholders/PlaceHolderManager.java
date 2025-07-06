package com.epherical.chatmanager.placeholders;

import com.epherical.chatmanager.util.PlaceHolderContext;
import net.minecraft.resources.ResourceLocation;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Registers and expands placeholders of the form
 *   {namespace:path[,param1,param2,...]}
 * before text is passed to the ComponentParser.
 */
public final class PlaceHolderManager {

    /* ----------  Supplier types ---------- */

    /** New supplier that can receive arbitrary parameters. */
    @FunctionalInterface
    public interface ParamSupplier {
        String apply(PlaceHolderContext context, String... params);
    }

    /**
     * Legacy supplier that ignores parameters.  All existing code
     * continues to work through the bridging overload of {@code register}.
     */
    @FunctionalInterface
    public interface SimpleSupplier {
        String apply(PlaceHolderContext context);
    }

    /* ----------  Registry & pattern ---------- */

    private static final Map<ResourceLocation, ParamSupplier> REGISTRY = new HashMap<>();

    /**
     * Captures everything between the braces.  We will split the result
     * on commas so we can support an arbitrary number of parameters.
     *
     * Examples it matches:
     *   {chatmanager:player}
     *   {chatmanager:player,x,y,z}
     */
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{([^{}]+)}");

    private PlaceHolderManager() {}

    /* ----------  Registration helpers ---------- */

    /** Register a parameter-aware supplier. */
    public static void register(ResourceLocation id, ParamSupplier supplier) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(supplier);
        if (REGISTRY.putIfAbsent(id, supplier) != null) {
            throw new IllegalStateException("Placeholder already registered: " + id);
        }
    }

    /** Bridge: register an old-style supplier that ignores parameters. */
    public static void register(ResourceLocation id, SimpleSupplier supplier) {
        register(id, (ctx, params) -> supplier.apply(ctx));
    }

    /* ----------  Expansion ---------- */

    public static String process(String text, PlaceHolderContext context) {
        final int RECURSION_LIMIT = 5;
        String current = text;

        for (int i = 0; i < RECURSION_LIMIT; i++) {
            Matcher matcher = PLACEHOLDER_PATTERN.matcher(current);
            StringBuffer sb = new StringBuffer();
            boolean anyFound = false;

            while (matcher.find()) {
                anyFound = true;

                String insideBraces = matcher.group(1).trim();
                String[] tokens = insideBraces.split("\\s*,\\s*");   // split on ',' and trim
                if (tokens.length == 0) {
                    matcher.appendReplacement(sb, Matcher.quoteReplacement(matcher.group(0)));
                    continue;
                }

                ResourceLocation id = ResourceLocation.tryParse(tokens[0]);
                String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);

                String replacement = matcher.group(0); // default: leave unchanged
                if (id != null) {
                    ParamSupplier supplier = REGISTRY.get(id);
                    if (supplier != null) {
                        String supplied = supplier.apply(context, params);
                        if (supplied != null) {
                            replacement = supplied;
                        }
                    }
                }
                matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
            }
            matcher.appendTail(sb);

            // stop if we neither found nor changed anything
            if (!anyFound || sb.toString().equals(current)) {
                break;
            }
            current = sb.toString();
        }
        return current;
    }
}
