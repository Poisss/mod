package com.hypixel.hytale.server.core.util;

public record Identifier(String namespace, String path) {
    public static Identifier of(String namespace, String path) {
        return new Identifier(namespace, path);
    }

    public static Identifier parse(String id) {
        String[] parts = id.split(":", 2);
        if (parts.length == 2) {
            return new Identifier(parts[0], parts[1]);
        }
        return new Identifier("hytale", parts[0]);
    }

    @Override
    public String toString() {
        return namespace + ":" + path;
    }
}
