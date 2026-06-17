package com.hypixel.hytale.server.core.registry;

import com.hypixel.hytale.server.core.util.Identifier;

public record RegistryKey<T>(Identifier identifier, Class<T> type) {
    public static <T> RegistryKey<T> of(Identifier id, Class<T> type) {
        return new RegistryKey<>(id, type);
    }
}
