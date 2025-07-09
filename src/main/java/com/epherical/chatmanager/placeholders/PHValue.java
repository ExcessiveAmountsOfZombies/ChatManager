package com.epherical.chatmanager.placeholders;

import net.minecraft.network.chat.MutableComponent;

/** One expanded placeholder – either text or a component. */
public sealed interface PHValue permits PHValue.Text, PHValue.Component {

    /** true when this value is the component variant. */
    boolean isComponent();

    /** Plain text (undefined when {@link #isComponent()} == true). */
    String asString();

    /** Component (undefined when {@link #isComponent()} == false). */
    MutableComponent asComponent();

    /* ------------------------------------------------------------------ */

    record Text(String value) implements PHValue {
        @Override public boolean isComponent()          { return false; }
        @Override public String asString()              { return value; }
        @Override public MutableComponent asComponent() { throw new IllegalStateException(); }
    }

    record Component(MutableComponent value) implements PHValue {
        @Override public boolean isComponent()          { return true; }
        @Override public String asString()              { throw new IllegalStateException(); }
        @Override public MutableComponent asComponent() { return value; }
    }

    /* helpers */
    static Text text(String s)                       { return new Text(s); }
    static Component comp(MutableComponent mc)       { return new Component(mc); }
}
