// TemplateParser.java
package com.epherical.chatmanager.util.text;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TemplateParser {

    private TemplateParser() {}

    private static final Pattern PLACEHOLDER =
            Pattern.compile("\\{([a-z0-9_.:-]+)(?:,([^{}]*))?}", Pattern.CASE_INSENSITIVE);

    public static TemplateNode.Container parse(String raw) {
        Objects.requireNonNull(raw);

        Deque<Frame> stack = new ArrayDeque<>();
        stack.push(new Frame(Style.EMPTY));

        int i = 0;
        while (i < raw.length()) {
            if (raw.startsWith("<comp", i)) {
                int end = raw.indexOf('>', i);
                if (end < 0) break;

                Style merged = mergeStyle(stack.peek().style, raw.substring(i, end + 1));
                stack.push(new Frame(merged));
                i = end + 1;
            } else if (raw.startsWith("</comp>", i)) {
                Frame child = stack.pop();
                stack.peek().children.add(
                        new TemplateNode.Container(child.style, child.children));
                i += "</comp>".length();
            } else {
                int nextToken = nextSpecial(raw, i);

                // Guarantee forward progress
                if (nextToken == i) {
                    nextToken++;               // skip the troublesome char
                }

                String segment = raw.substring(i, nextToken);

                // inside that segment there may still be {placeholders}
                Matcher m = PLACEHOLDER.matcher(segment);
                int last = 0;
                while (m.find()) {
                    if (m.start() > last) {
                        stack.peek().children.add(
                                new TemplateNode.Text(segment.substring(last, m.start()),
                                                      stack.peek().style));
                    }

                    var id = ResourceLocation.parse(m.group(1));
                    String[] params = Optional.ofNullable(m.group(2))
                                              .filter(s -> !s.isEmpty())
                                              .map(s -> s.split(",", -1))
                                              .orElseGet(() -> new String[0]);

                    stack.peek().children.add(
                            new TemplateNode.Placeholder(id, params, stack.peek().style));

                    last = m.end();
                }
                if (last < segment.length()) {
                    stack.peek().children.add(
                            new TemplateNode.Text(segment.substring(last),
                                                  stack.peek().style));
                }

                i = nextToken;                 // ↰ 'i' is now strictly larger
            }
        }
        if (stack.size() != 1) {
            throw new IllegalStateException("Unclosed <comp> tags");
        }
        Frame root = stack.pop();
        return new TemplateNode.Container(root.style, root.children);
    }

    /* --------------------- helpers ----------------------- */

    private record Frame(Style style, List<TemplateNode> children) {
        Frame(Style s) { this(s, new ArrayList<>()); }
    }

    private static int nextSpecial(String s, int from) {
        int lt = s.indexOf('<', from + 1);
        int lb = s.indexOf('{', from + 1);
        if (lt == -1) return (lb == -1) ? s.length() : lb;
        if (lb == -1) return lt;
        return Math.min(lt, lb);
    }

    private static Style mergeStyle(Style parent, String tag) {
        Map<String,String> attrs = parseAttributes(tag);
        Style st = parent;
        if (attrs.containsKey("color")) st = st.withColor(parseColor(attrs.get("color")));
        if (attrs.containsKey("font"))
            st = st.withFont(net.minecraft.resources.ResourceLocation.parse(attrs.get("font")));
        if (attrs.containsKey("style")) {
            for (String part : attrs.get("style").split(",")) {
                switch (part.trim().toLowerCase(Locale.ROOT)) {
                    case "bold"      -> st = st.withBold(true);
                    case "italic"    -> st = st.withItalic(true);
                    case "underlined"-> st = st.withUnderlined(true);
                    case "strikethrough" -> st = st.withStrikethrough(true);
                    case "obfuscated"-> st = st.withObfuscated(true);
                }
            }
        }
        return st;
    }

    private static Map<String,String> parseAttributes(String tag) {
        Map<String,String> out = new HashMap<>();
        Matcher m = Pattern.compile("(\\w+)\\s*=\\s*'([^']*)'").matcher(tag);
        while (m.find()) out.put(m.group(1).toLowerCase(Locale.ROOT), m.group(2));
        return out;
    }

    private static TextColor parseColor(String raw) {
        raw = raw.trim();
        ChatFormatting named = ChatFormatting.getByName(raw.replace('#',' ').trim());
        if (named != null) return TextColor.fromLegacyFormat(named);
        if (raw.startsWith("#") && raw.length() == 7) {
            int rgb = FastColor.ARGB32.color(255,
                    Integer.parseInt(raw.substring(1,3),16),
                    Integer.parseInt(raw.substring(3,5),16),
                    Integer.parseInt(raw.substring(5,7),16));
            return TextColor.fromRgb(rgb);
        }
        return TextColor.fromLegacyFormat(ChatFormatting.WHITE);
    }
}
