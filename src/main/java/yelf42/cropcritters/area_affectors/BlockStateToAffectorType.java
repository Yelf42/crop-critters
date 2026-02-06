package yelf42.cropcritters.area_affectors;

import net.minecraft.block.BlockState;
import net.minecraft.block.enums.DoubleBlockHalf;
import org.jspecify.annotations.Nullable;
import yelf42.cropcritters.blocks.ModBlocks;
import yelf42.cropcritters.blocks.SoulRoseBlock;
import yelf42.cropcritters.blocks.SoulRoseType;

// Converts block state to affector type
public class BlockStateToAffectorType {

    public static @Nullable AffectorType getTypeFromBlockState(BlockState state) {
        if (state.isOf(ModBlocks.SOUL_ROSE) && state.get(SoulRoseBlock.HALF, DoubleBlockHalf.UPPER) == DoubleBlockHalf.LOWER) {
            int level = state.get(SoulRoseBlock.LEVEL, 0);
            SoulRoseType type = state.get(SoulRoseBlock.TYPE, SoulRoseType.NONE);
            return switch (level) {
                case 1 -> switch (type) {
                    case GOLD -> AffectorType.SOUL_ROSE_GOLD_1;
                    case COPPER -> AffectorType.SOUL_ROSE_COPPER_1;
                    case IRON -> AffectorType.SOUL_ROSE_IRON_1;
                    default -> null;
                };
                case 2 -> switch (type) {
                    case GOLD -> AffectorType.SOUL_ROSE_GOLD_2;
                    case COPPER -> AffectorType.SOUL_ROSE_COPPER_2;
                    case IRON -> AffectorType.SOUL_ROSE_IRON_2;
                    default -> null;
                };
                case 3 -> switch (type) {
                    case GOLD -> AffectorType.SOUL_ROSE_GOLD_3;
                    case COPPER -> AffectorType.SOUL_ROSE_COPPER_3;
                    case IRON -> AffectorType.SOUL_ROSE_IRON_3;
                    default -> null;
                };
                default -> null;
            };
        }
        return null;
    }

}
