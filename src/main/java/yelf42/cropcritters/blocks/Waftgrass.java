package yelf42.cropcritters.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCollisionHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import yelf42.cropcritters.CropCritters;

public class Waftgrass extends SpreadingWeedBlock {

    public Waftgrass(Settings settings) {
        super(settings);
    }

    @Override
    protected boolean canPlantOnTop(BlockState floor, BlockView world, BlockPos pos) {
        return super.canPlantOnTop(floor, world, pos) || floor.isOf(Blocks.CRIMSON_NYLIUM) || floor.isOf(Blocks.WARPED_NYLIUM) || floor.isOf(Blocks.NETHERRACK);
    }

    @Override
    public int getMaxNeighbours() { return 3; }

    @Override
    protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity, EntityCollisionHandler handler, boolean bl) {
        // Apply damage, avoid critters and nether mobs
        if (world instanceof ServerWorld serverWorld
                && entity instanceof LivingEntity livingEntity
                && !(livingEntity.getType().isIn(CropCritters.CROP_CRITTERS))) {
            livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION, 120));
        }
    }
}
