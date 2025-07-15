package yelf42.cropcritters.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCollisionHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import yelf42.cropcritters.CropCritters;

public class WitheringSpiteweed extends SpreadingWeekBlock {

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
    public int getMaxNeighbours() { return 3; }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        BlockState soilCheck = world.getBlockState(pos.down());
        if (soilCheck.isOf(Blocks.SOUL_SAND) || soilCheck.isOf(Blocks.SOUL_SOIL) || soilCheck.isOf(ModBlocks.SOUL_FARMLAND)) {
            world.setBlockState(pos.down(), Blocks.BLACKSTONE.getDefaultState(), Block.NOTIFY_LISTENERS);
        }
    }

    @Override
    protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity, EntityCollisionHandler handler) {
        Vec3d vec3d = new Vec3d(0.9, 0.9F, 0.9);
        entity.slowMovement(state, vec3d);

        // Apply damage, avoid critters and nether mobs
        if (world instanceof ServerWorld serverWorld
                && !(entity.getType().isIn(CropCritters.WEED_IMMUNE))) {
            entity.damage(serverWorld, world.getDamageSources().sweetBerryBush(), 1.0F);
        }
    }
}
