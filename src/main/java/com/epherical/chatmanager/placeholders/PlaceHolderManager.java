package com.epherical.chatmanager.placeholders;

import com.epherical.chatmanager.util.PlaceHolderContext;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Registers and expands placeholders of the form {namespace:path}
 * before text is passed to ComponentParser.
 */
public final class PlaceHolderManager {

    @FunctionalInterface
    public interface PlaceHolderSupplier {
        String apply(PlaceHolderContext context);
    }

    private static final Map<ResourceLocation, PlaceHolderSupplier> REGISTRY = new HashMap<>();
    private static final Pattern PLACEHOLDER_PATTERN =
            Pattern.compile("\\{([a-z0-9_\\-:.]+)}");

    private PlaceHolderManager() {}

    public static void register(ResourceLocation id, PlaceHolderSupplier supplier) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(supplier);
        if (REGISTRY.putIfAbsent(id, supplier) != null) {
            throw new IllegalStateException("Placeholder already registered: " + id);
        }
    }

    public static String process(String text, PlaceHolderContext context) {
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
                    PlaceHolderSupplier supp = REGISTRY.get(id);
                    if (supp != null) {
                        replacement = supp.apply(context);
                    }
                }
                if (replacement == null) replacement = m.group(0);
                m.appendReplacement(sb, Matcher.quoteReplacement(replacement));
            }
            m.appendTail(sb);
            if (!found || sb.toString().equals(current)) break;
            current = sb.toString();
        }
        return current;
    }
}
