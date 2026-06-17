package com.hypixel.hytale.server.core.text;

public enum TextColor {
    WHITE("white"), BLACK("black"), GRAY("gray"), DARK_GRAY("dark_gray"),
    RED("red"), GREEN("green"), BLUE("blue"), AQUA("aqua"),
    YELLOW("yellow"), GOLD("gold"), LIGHT_PURPLE("light_purple"),
    DARK_PURPLE("dark_purple"), DARK_RED("dark_red"), DARK_GREEN("dark_green"),
    DARK_BLUE("dark_blue"), DARK_AQUA("dark_aqua"), DARK_YELLOW("dark_yellow"),
    LIGHT_GRAY("light_gray"), RESET("reset");

    private final String name;
    TextColor(String name) { this.name = name; }

    @Override
    public String toString() { return ""; }
}
