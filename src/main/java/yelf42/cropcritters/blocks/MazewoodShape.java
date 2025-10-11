package yelf42.cropcritters.blocks;

import net.minecraft.util.StringIdentifiable;

public enum MazewoodShape implements StringIdentifiable {
    NONE("none"),
    TALL("tall");

    private final String name;

    private MazewoodShape(final String name) {
        this.name = name;
    }

    public String toString() {
        return this.asString();
    }

    public String asString() {
        return this.name;
    }
}
