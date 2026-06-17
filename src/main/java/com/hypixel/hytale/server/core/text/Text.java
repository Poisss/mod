package com.hypixel.hytale.server.core.text;

public class Text {
    private final String content;

    private Text(String content) { this.content = content; }

    public static Text of(String content) { return new Text(content); }
    public static Text of(String... parts) { return new Text(String.join("", parts)); }

    @Override
    public String toString() { return content; }
}
