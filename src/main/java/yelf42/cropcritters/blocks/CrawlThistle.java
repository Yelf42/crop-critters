package yelf42.cropcritters.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCollisionHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class CrawlThistle extends SpreadingWeedBlock {

    public CrawlThistle(Settings settings) {
        super(settings);
    }

    @Override
    public int getMaxNeighbours() { return 2; }

    @Override
    protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity, EntityCollisionHandler handler, boolean bl) {
        if (entity instanceof LivingEntity livingEntity) {
            Vec3d vec3d = new Vec3d(0.9, 0.9F, 0.9);
            livingEntity.slowMovement(state, vec3d);
        }
    }


}
