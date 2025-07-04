package com.epherical.chatmanager.compat;

import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.ModList;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ChatHeadsReflector {

    private static boolean attemptedLookup = false;
    private static boolean available = false;
    private static Method handleAddedMessageMethod = null;
    private static Method handleLineOwner = null;
    private static Field lastSenderData;

    private static void ensureInitialized() {
        if (attemptedLookup) return;
        attemptedLookup = true;

        // Only try lookup if the mod is loaded
        if (ModList.get().isLoaded("chat_heads")) {
            try {
                Class<?> chatHeadsClass = Class.forName("dzwdz.chat_heads.ChatHeads");
                handleAddedMessageMethod = chatHeadsClass.getMethod(
                        "handleAddedMessage", Component.class, ChatType.Bound.class, PlayerInfo.class);
                handleLineOwner = chatHeadsClass.getMethod("getLineData");

                lastSenderData = chatHeadsClass.getField("lastSenderData");

                available = true;
            } catch (Throwable t) {
                // Remain unavailable if class or method is missing
                available = false;
            }
        }
    }

    public static void handleAddedMessage(Component message, ChatType.Bound bound, PlayerInfo info) {
        ensureInitialized();
        if (available) {
            try {
                handleAddedMessageMethod.invoke(null, message, bound, info);
            } catch (Throwable t) {
                // Swallow errors to remain safe
            }
        }
    }

    public static Object getLastSenderData() {
        ensureInitialized();
        if (available) {
            try {
                return lastSenderData.get(null);
            } catch (IllegalAccessException ignored) {

            }
        }

        return null;
    }

    public static void setLastSenderData(Object data) {
        ensureInitialized();
        if (available) {
            try {
                lastSenderData.set(null, data);
            } catch (IllegalAccessException ignored) {

            }
        }
    }


    public static boolean isAvailable() {
        ensureInitialized();
        return available;
    }
}
