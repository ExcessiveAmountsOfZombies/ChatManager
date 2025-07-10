// TemplateNode.java
package com.epherical.chatmanager.util.text;

import com.epherical.chatmanager.placeholders.PlaceHolderManager;
import com.epherical.chatmanager.util.PlaceHolderContext;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;

public sealed interface TemplateNode
        permits TemplateNode.Container, TemplateNode.Text, TemplateNode.Placeholder {

    MutableComponent emit(PlaceHolderContext ctx);

    /* ------------------------------------------------------ */

    record Container(Style style,
                     java.util.List<TemplateNode> children) implements TemplateNode {

        @Override public MutableComponent emit(PlaceHolderContext ctx) {
            var root = Component.empty();
            for (TemplateNode n : children) root.append(n.emit(ctx));
            return root.withStyle(style);
        }
    }

    record Text(String raw, Style style) implements TemplateNode {
        @Override public MutableComponent emit(PlaceHolderContext ctx) {
            return net.minecraft.network.chat.Component.literal(raw).withStyle(style);
        }
    }

    record Placeholder(ResourceLocation id,
                       String[] params,
                       net.minecraft.network.chat.Style style) implements TemplateNode {

        @Override public MutableComponent emit(PlaceHolderContext ctx) {
            MutableComponent resolved =
                    PlaceHolderManager.resolve(id, ctx, params);
            return resolved.withStyle(style.applyTo(resolved.getStyle()));
        }
    }
}
