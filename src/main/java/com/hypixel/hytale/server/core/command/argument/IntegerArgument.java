package com.hypixel.hytale.server.core.command.argument;

public class IntegerArgument implements Argument<Integer> {
    public static Argument<Integer> of(String name) { return new IntegerArgument(); }
}
