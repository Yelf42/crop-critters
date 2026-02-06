package yelf42.cropcritters.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.StringIdentifiable;
import org.jetbrains.annotations.Nullable;

public enum SoulRoseType implements StringIdentifiable {
    NONE("none"),
    GOLD("gold"),
    COPPER("copper"),
    IRON("iron");

    private final String name;

    private SoulRoseType(final String name) {
        this.name = name;
    }

    public static SoulRoseType getType(BlockState state, int level) {
        if (level == 0) return NONE;
        if (state.isOf(Blocks.RAW_GOLD_BLOCK)) return GOLD;
        if (state.isOf(Blocks.RAW_COPPER_BLOCK)) return COPPER;
        if (state.isOf(Blocks.RAW_IRON_BLOCK)) return IRON;

        return NONE;
    }

    public String toString() {
        return this.name;
    }

    public String asString() {
        return this.name;
    }
}