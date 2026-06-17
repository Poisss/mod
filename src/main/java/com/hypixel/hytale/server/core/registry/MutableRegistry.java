package com.hypixel.hytale.server.core.registry;

public interface MutableRegistry<T> {
    void register(RegistryKey<T> key, T value, boolean override);
}
