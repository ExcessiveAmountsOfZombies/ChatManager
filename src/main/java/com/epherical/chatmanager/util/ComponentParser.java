package com.epherical.chatmanager.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Usage:
 *   MutableComponent comp =
 *       ComponentParser.parse("""
 *            <comp color="#434343" style="bold,italic" font="minecraft:font2">
 *                Howdy
 *                <comp color="#000000">Bitch</comp>
 *            </comp>
 *       """);
 */
public final class ComponentParser {

    /* ---------------------------------------------------------------------- */
    /* Public API                                                             */
    /* ---------------------------------------------------------------------- */

    public static MutableComponent parse(String raw) {
        Objects.requireNonNull(raw);

        Deque<Frame> stack = new ArrayDeque<>();
        // bottom-most holder – inherits Style.EMPTY
        stack.push(new Frame(Style.EMPTY));

        int i = 0;
        while (i < raw.length()) {
            if (raw.startsWith("<comp", i)) {
                // opening tag
                int tagEnd = raw.indexOf('>', i);
                if (tagEnd < 0) break; // malformed – bail out

                String tag = raw.substring(i, tagEnd + 1);
                Map<String, String> attrs = parseAttributes(tag);

                Style parent = stack.peek().style;
                Style merged = mergeStyle(parent, attrs);

                stack.push(new Frame(merged));
                i = tagEnd + 1;
            } else if (raw.startsWith("</comp>", i)) {
                // closing tag
                Frame child = stack.pop();
                if (stack.isEmpty()) {
                    throw new IllegalStateException("Too many </comp> tags");
                }
                stack.peek().component.append(child.component);
                i += "</comp>".length();
            } else {
                // plain text
                int nextTag = raw.indexOf('<', i);
                String text = (nextTag == -1) ? raw.substring(i) : raw.substring(i, nextTag);
                stack.peek().component.append(Component.literal(text).withStyle(stack.peek().style));
                i = (nextTag == -1) ? raw.length() : nextTag;
            }
        }

        if (stack.size() != 1) {
            throw new IllegalStateException("Unclosed <comp> tags in input");
        }
        return stack.pop().component;
    }

    /* ---------------------------------------------------------------------- */
    /* Helpers                                                                */
    /* ---------------------------------------------------------------------- */

    private static Map<String, String> parseAttributes(String tag) {
        // Very small/tolerant attribute extractor: key="value"
        Map<String, String> map = new HashMap<>();
        Matcher m = ATTR_PATTERN.matcher(tag);
        while (m.find()) {
            map.put(m.group(1).toLowerCase(Locale.ROOT), m.group(2));
        }
        return map;
    }

    private static Style mergeStyle(Style parent, Map<String, String> attrs) {
        Style style = parent;

        // color
        if (attrs.containsKey("color")) {
            style = style.withColor(parseColor(attrs.get("color")));
        }

        // font
        if (attrs.containsKey("font")) {
            style = style.withFont((ResourceLocation.parse(attrs.get("font"))));
        }

        // style flags (bold, italic, underlined, strikethrough, obfuscated)
        if (attrs.containsKey("style")) {
            String[] parts = attrs.get("style").split("\\s*,\\s*");
            for (String p : parts) {
                switch (p.toLowerCase(Locale.ROOT)) {
                    case "bold"          -> style = style.withBold(true);
                    case "italic"        -> style = style.withItalic(true);
                    case "underlined"    -> style = style.withUnderlined(true);
                    case "strikethrough" -> style = style.withStrikethrough(true);
                    case "obfuscated"    -> style = style.withObfuscated(true);
                    default              -> { /* ignore unknown */ }
                }
            }
        }
        return style;
    }

    private static TextColor parseColor(String raw) {
        raw = raw.trim();
        // Attempt named formatting first (e.g. "red", "dark_blue")
        ChatFormatting named = ChatFormatting.getByName(raw.replace('#', ' ').trim());
        if (named != null) return TextColor.fromLegacyFormat(named);

        // hex format #rrggbb
        if (raw.startsWith("#") && raw.length() == 7) {
            int rgb = FastColor.ARGB32.color(255,
                    Integer.valueOf(raw.substring(1, 3), 16),
                    Integer.valueOf(raw.substring(3, 5), 16),
                    Integer.valueOf(raw.substring(5, 7), 16));
            return TextColor.fromRgb(rgb);
        }
        return TextColor.fromLegacyFormat(ChatFormatting.WHITE); // fallback
    }

    /** Small holder class sitting on the stack while we parse. */
    private record Frame(Style style, MutableComponent component) {
        Frame(Style style) { this(style, Component.empty()); }
    }

    private static final Pattern ATTR_PATTERN =
            Pattern.compile("(\\w+)\\s*=\\s*'([^']*)'");
}
