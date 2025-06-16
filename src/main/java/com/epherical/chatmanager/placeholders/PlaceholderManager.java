package com.epherical.chatmanager.placeholders;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Registers and expands placeholders of the form {namespace:path}
 * before text is passed to ComponentParser.
 */
public final class PlaceholderManager {

    @FunctionalInterface
    public interface PlaceholderSupplier {
        /**
         * @param player the player the text is being generated for (may be null
         *               if the context is console / broadcast)
         * @return replacement string (may contain further <comp> tags)
         */
        String apply(ServerPlayer player);
    }

    /* ------------------------------------------------------------ */

    private static final Map<ResourceLocation, PlaceholderSupplier> REGISTRY = new HashMap<>();
    private static final Pattern PLACEHOLDER_PATTERN =
            Pattern.compile("\\{([a-z0-9_\\-:.]+)}"); // {namespace:path}

    private PlaceholderManager() {}

    public static void register(ResourceLocation id, PlaceholderSupplier supplier) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(supplier);
        if (REGISTRY.putIfAbsent(id, supplier) != null) {
            throw new IllegalStateException("Placeholder already registered: " + id);
        }
    }

    /**
     * Replaces all known placeholders in the given text.
     * Unknown placeholders are left as-is.
     */
    public static String process(String text, ServerPlayer player) {
        // Appropriately handle up to some recursion limit (e.g., 5 passes)
        final int RECURSION_LIMIT = 5;
        String current = text;
        for (int i = 0; i < RECURSION_LIMIT; i++) {
            Matcher m = PLACEHOLDER_PATTERN.matcher(current);
            StringBuffer sb = new StringBuffer();
            boolean found = false;
            while (m.find()) {
                found = true;
                ResourceLocation id = ResourceLocation.tryParse(m.group(1));
                String replacement = null;
                if (id != null) {
                    PlaceholderSupplier supp = REGISTRY.get(id);
                    if (supp != null) {
                        replacement = supp.apply(player);
                    }
                }
                if (replacement == null) replacement = m.group(0); // fallback: leave placeholder
                m.appendReplacement(sb, Matcher.quoteReplacement(replacement));
            }
            m.appendTail(sb);
            if (!found) {
                // No placeholders found this pass, stop
                break;
            }
            if (sb.toString().equals(current)) {
                // No change, stop
                break;
            }
            current = sb.toString();
        }
        return current;
    }
}
