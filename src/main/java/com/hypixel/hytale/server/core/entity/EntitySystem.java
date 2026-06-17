package com.hypixel.hytale.server.core.entity;

public interface EntitySystem {
    default void update(World world, float deltaTime) {}
}
