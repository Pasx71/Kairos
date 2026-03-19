package com.kairos.api.seasons;

public enum Season {
    SPRING("Spring", "&a"),
    SUMMER("Summer", "&e"),
    AUTUMN("Autumn", "&6"),
    WINTER("Winter", "&b");

    private final String name;
    private final String color;

    Season(String name, String color) {
        this.name = name;
        this.color = color;
    }

    public String getName() { return name; }
    public String getColor() { return color; }
    public String getDisplayName() { return color + name; }
}