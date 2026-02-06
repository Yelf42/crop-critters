package yelf42.cropcritters.entity;

import net.minecraft.block.*;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Colors;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import yelf42.cropcritters.CropCritters;
import yelf42.cropcritters.blocks.ModBlocks;
import yelf42.cropcritters.items.ModItems;

public class HerbicideEntity extends ThrownItemEntity {

    public HerbicideEntity(EntityType<? extends ThrownItemEntity> entityType, World world) {
        super(entityType, world);
    }

    public HerbicideEntity(double x, double y, double z, World world, ItemStack stack) {
        super(ModEntities.HERBICIDE_PROJECTILE, x, y, z, world, stack);
    }

    public HerbicideEntity(ServerWorld serverWorld, LivingEntity livingEntity, ItemStack itemStack) {
        super(ModEntities.HERBICIDE_PROJECTILE, livingEntity, serverWorld, itemStack);
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);
        this.getEntityWorld().sendEntityStatus(this, (byte)3);
        if (this.getEntityWorld() instanceof ServerWorld serverWorld) {
            killWeeds(serverWorld, this.getBlockPos());
            serverWorld.syncWorldEvent(2002, this.getBlockPos(), Colors.GREEN);
            this.discard();
        }
    }

    private void killWeeds(ServerWorld world, BlockPos blockPos) {
        Iterable<BlockPos> iterable = BlockPos.iterateOutwards(blockPos, 4, 4, 4);

        for (BlockPos pos : iterable) {
            if (!pos.isWithinDistance(this.getEntityPos(), 4)) continue;

            BlockState check = world.getBlockState(pos);
            boolean airState = check.getFluidState().isEmpty();
            if (check.isIn(CropCritters.WEEDS) && !check.isOf(ModBlocks.PUFFBOMB_MUSHROOM)) {
                world.setBlockState(pos, airState ? Blocks.AIR.getDefaultState() : Blocks.WATER.getDefaultState(), 3);
            }
        }
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.HERBICIDE;
    }

    @Override
    protected double getGravity() {
        return 0.04;
    }
}
