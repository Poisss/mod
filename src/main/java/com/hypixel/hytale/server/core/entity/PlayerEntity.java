package com.hypixel.hytale.server.core.entity;

import com.hypixel.hytale.server.core.util.math.Vec3i;

public class PlayerEntity {
    public String getName() { return ""; }
    public java.util.UUID getUuid() { return java.util.UUID.randomUUID(); }
    public void sendMessage(com.hypixel.hytale.server.core.text.Text text) {}
    public void sendMessage(String text) {}
    public void teleport(World world, int x, int y, int z) {}
}
