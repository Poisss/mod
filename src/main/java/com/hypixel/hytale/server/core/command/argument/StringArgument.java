package com.hypixel.hytale.server.core.command.argument;

public class StringArgument implements Argument<String> {
    public static Argument<String> of(String name) { return new StringArgument(); }
}
