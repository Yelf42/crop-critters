package yelf42.cropcritters.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCollisionHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import yelf42.cropcritters.CropCritters;

public class CrimsonThornweed extends SpreadingWeekBlock {

    public CrimsonThornweed(Settings settings) {
        super(settings);
    }

    @Override
    public int getMaxNeighbours() { return 3; }

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
