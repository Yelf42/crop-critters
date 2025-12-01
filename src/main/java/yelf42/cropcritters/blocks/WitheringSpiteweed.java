package yelf42.cropcritters.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCollisionHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import yelf42.cropcritters.CropCritters;

public class WitheringSpiteweed extends SpreadingWeedBlock {

    public WitheringSpiteweed(Settings settings) {
        super(settings);
    }

    @Override
    protected boolean canPlantOnTop(BlockState floor, BlockView world, BlockPos pos) {
        return super.canPlantOnTop(floor, world, pos)
                || floor.isOf(Blocks.SOUL_SOIL)
                || floor.isOf(Blocks.SOUL_SAND)
                || floor.isOf(Blocks.BLACKSTONE)
                || floor.isOf(Blocks.CRIMSON_NYLIUM)
                || floor.isOf(Blocks.WARPED_NYLIUM)
                || floor.isOf(Blocks.NETHERRACK);
    }

    @Override
    protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        BlockState soilCheck = world.getBlockState(pos.down());
        if (!soilCheck.isOf(Blocks.BLACKSTONE) && (soilCheck.isOf(Blocks.SOUL_SAND) || soilCheck.isOf(Blocks.SOUL_SOIL) || soilCheck.isOf(ModBlocks.SOUL_FARMLAND))) {
            world.setBlockState(pos.down(), Blocks.BLACKSTONE.getDefaultState(), Block.NOTIFY_LISTENERS);
        }
        super.randomTick(state, world, pos, random);
    }

    @Override
    public int getMaxNeighbours() { return 3; }

    // Also turn the block below in blackstone if possible
    @Override
    public void setToWeed(World world, BlockPos pos) {
        super.setToWeed(world, pos);
        BlockState soilCheck = world.getBlockState(pos.down());
        if (soilCheck.isOf(Blocks.SOUL_SAND) || soilCheck.isOf(Blocks.SOUL_SOIL) || soilCheck.isOf(ModBlocks.SOUL_FARMLAND)) {
            world.setBlockState(pos.down(), Blocks.BLACKSTONE.getDefaultState(), Block.NOTIFY_LISTENERS);
        }
    }

    @Override
    protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity, EntityCollisionHandler handler, boolean bl) {
        if (world instanceof ServerWorld serverWorld
                && entity instanceof LivingEntity livingEntity
                && !(livingEntity.getType().isIn(CropCritters.CROP_CRITTERS))) {
            Vec3d vec3d = new Vec3d(0.9, 0.9F, 0.9);
            livingEntity.slowMovement(state, vec3d);
            if (!livingEntity.isInvulnerableTo(serverWorld, world.getDamageSources().wither())) {
                livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 40));

            }
        }
    }
}
