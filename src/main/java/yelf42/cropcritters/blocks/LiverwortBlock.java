package yelf42.cropcritters.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.*;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.attribute.EnvironmentAttributes;
import yelf42.cropcritters.config.WeedPlacement;

import java.awt.*;

public class LiverwortBlock extends MultifaceGrowthBlock implements Fertilizable {
    public static final MapCodec<LiverwortBlock> CODEC = createCodec(LiverwortBlock::new);
    private final MultifaceGrower grower = new MultifaceGrower(new LiverwortGrowChecker(this));

    public MapCodec<LiverwortBlock> getCodec() {
        return CODEC;
    }

    public LiverwortBlock(AbstractBlock.Settings settings) {
        super(settings);
    }

    @Override
    protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        if (super.canPlaceAt(state, world, pos)) return true;

        boolean bl = false;
        for(Direction direction : DIRECTIONS) {
            if (hasDirection(state, direction)) {
                if (!world.getBlockState(pos.offset(direction)).isIn(BlockTags.DIRT)) {
                    return false;
                }
                bl = true;
            }
        }
        return bl;
    }

    @Override
    public boolean canGrowWithDirection(BlockView world, BlockState state, BlockPos pos, Direction direction) {
        if (super.canGrowWithDirection(world, state, pos, direction)) return true;
        if (this.canHaveDirection(direction) && (!state.isOf(this) || !hasDirection(state, direction))) {
            BlockPos blockPos = pos.offset(direction);
            return world.getBlockState(blockPos).isIn(BlockTags.DIRT);
        } else {
            return false;
        }
    }

    @Override
    protected boolean hasRandomTicks(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {

        // Waterlogged growth
        if (!state.getFluidState().isEmpty() && world.getLightLevel(LightType.SKY, pos) >= 13) {
            if (random.nextInt(4) == 0 && isFertilizable(world, pos, state)) this.grower.grow(state, world, pos, random);
            return;
        }

        // Rain growth
        if (world.hasRain(pos) || world.hasRain(pos.offset(Direction.random(random)))) {
            if (random.nextInt(2) == 0 && isFertilizable(world, pos, state)) this.grower.grow(state, world, pos, random);
            return;
        }

        // Moist farmland
        BlockState soil = world.getBlockState(pos.down());
        if (soil.isOf(ModBlocks.SOUL_FARMLAND) || (soil.isOf(Blocks.FARMLAND) && soil.get(FarmlandBlock.MOISTURE, 0) > 5)) {
            if (isFertilizable(world, pos, state)) this.grower.grow(state, world, pos, random);
            return;
        }

        // Dry out in sunlight
        long time = world.getTimeOfDay() % 24000;
        if (state.getFluidState().isEmpty()
                && (time <= 8000 && time >= 4000)
                && (random.nextInt(2) == 0)
                && world.getLightLevel(LightType.SKY, pos) >= 15) {
            world.addSyncedBlockEvent(pos, this, 0, 0);
            return;
        }

        // Dry out in nether (any dimension where water evaporates)
        if (world.getEnvironmentAttributes().getAttributeValue(EnvironmentAttributes.WATER_EVAPORATES_GAMEPLAY, pos)) {
            world.addSyncedBlockEvent(pos, this, 0, 0);
            return;
        }

        // Dripping growth / death
        BlockPos blockPos = PointedDripstoneBlock.getDripPos(world, pos);
        if (blockPos != null) {
            Fluid fluid = PointedDripstoneBlock.getDripFluid(world, blockPos);
            if (fluid == Fluids.WATER && isFertilizable(world, pos, state)) {
                this.grower.grow(state, world, pos, random);
            } else if (fluid == Fluids.LAVA) {
                world.addSyncedBlockEvent(pos, this, 0, 0);
            }
        }

    }

    @Override
    protected boolean onSyncedBlockEvent(BlockState state, World world, BlockPos pos, int type, int data) {
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();
        for(int l = 0; l < 8; ++l) {
            world.addParticleClient(ParticleTypes.WHITE_SMOKE, (double)((float)i + world.random.nextFloat()), (double)((float)j + world.random.nextFloat()), (double)((float)k + world.random.nextFloat()), (double)0.0F, (double)0.0F, (double)0.0F);
        }
        world.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
        return true;
    }

    public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state) {
        return Direction.stream().anyMatch((direction) -> this.grower.canGrow(state, world, pos, direction.getOpposite()));
    }

    public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
        return true;
    }

    public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
        this.grower.grow(state, world, pos, random);
    }

    protected boolean isTransparent(BlockState state) {
        return state.getFluidState().isEmpty();
    }

    public MultifaceGrower getGrower() {
        return this.grower;
    }

    static class LiverwortGrowChecker extends MultifaceGrower.LichenGrowChecker {

        public LiverwortGrowChecker(MultifaceBlock lichen) {
            super(lichen);
        }

        @Override
        protected boolean canGrow(BlockView world, BlockPos pos, BlockPos growPos, Direction direction, BlockState state) {
            return super.canGrow(world, pos, growPos, direction, state) || WeedPlacement.canWeedsReplace(state);
        }
    }

}
