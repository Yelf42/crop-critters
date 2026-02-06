package yelf42.cropcritters.features;

import com.mojang.serialization.Codec;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;
import yelf42.cropcritters.blocks.SoulRoseBlockEntity;

public class SoulRoseHintFeature extends Feature<DefaultFeatureConfig> {

    public SoulRoseHintFeature(Codec<DefaultFeatureConfig> configCodec) {
        super(configCodec);
    }

    @Override
    public boolean generate(FeatureContext<DefaultFeatureConfig> context) {
        Random random = context.getRandom();
        int level = random.nextInt(3) + 1;
        StructureWorldAccess world = context.getWorld();
        if (!world.getBlockState(context.getOrigin()).isOf(Blocks.AIR)) return false;
        BlockPos pos = context.getOrigin().down();

        // Stage 1
        if (!trySetBlock(world, pos.down().down(), random)) return false;
        for (Vec3i offset : SoulRoseBlockEntity.STAGE_1A) {
            if (!trySetBlock(world, pos.add(offset), random)) return true;
        }
        for (int i = 0; i < 4; i++) {
            for (Vec3i offset : SoulRoseBlockEntity.STAGE_1B) {
                trySetBlock(world, pos.add(rotate(offset, i)), random);
            }
        }

        // Stage 2
        if (level > 1) {
            for (int i = 0; i < 4; i++) {
                for (Vec3i offset : SoulRoseBlockEntity.STAGE_2) {
                    trySetBlock(world, pos.add(rotate(offset, i)), random);
                }
            }
        }

        // Stage 3
        if (level > 2) {
            for (int i = 0; i < 4; i++) {
                for (Vec3i offset : SoulRoseBlockEntity.STAGE_3) {
                    trySetBlock(world, pos.add(rotate(offset, i)), random);
                }
            }
        }

        return true;
    }

    private boolean trySetBlock(StructureWorldAccess world, BlockPos pos, Random random) {
        BlockState check = world.getBlockState(pos);
        if (!check.isOf(Blocks.AIR)) {
            this.setBlockState(world, pos, chooseBlock(random));
            return true;
        }
        return false;
    }

    private static BlockState chooseBlock(Random random) {
        //return Blocks.END_ROD.getDefaultState();
        return random.nextInt(5) != 0 ? Blocks.GILDED_BLACKSTONE.getDefaultState() : (random.nextInt(2) == 0 ? Blocks.BLACKSTONE.getDefaultState() : Blocks.RAW_GOLD_BLOCK.getDefaultState());
    }

    private static Vec3i rotate(Vec3i v, int dir) {
        int x = v.getX();
        int y = v.getY();
        int z = v.getZ();

        return switch (dir) {
            case 0  -> new Vec3i( x, y,  z);
            case 1 -> new Vec3i(-z, y,  x);
            case 2  -> new Vec3i(-x, y, -z);
            case 3 -> new Vec3i( z, y, -x);
            default -> v;
        };
    }
}
