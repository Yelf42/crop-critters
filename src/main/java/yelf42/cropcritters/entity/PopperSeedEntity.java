package yelf42.cropcritters.entity;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import yelf42.cropcritters.blocks.ModBlocks;
import yelf42.cropcritters.config.WeedPlacement;

public class PopperSeedEntity extends ThrownItemEntity {
    private int lifespan = 0;

    public PopperSeedEntity(EntityType<? extends ThrownItemEntity> entityType, World world) {
        super(entityType, world);
    }

    public PopperSeedEntity(Vec3d pos, World world) {
        super(ModEntities.POPPER_SEED_PROJECTILE, pos.x, pos.y, pos.z, world, new ItemStack(ModBlocks.POPPER_PLANT));
    }

    @Override
    protected Item getDefaultItem() {
        return ModBlocks.POPPER_PLANT.asItem();
    }

    @Override
    public void tick() {
        super.tick();
        ++this.lifespan;
        if (this.lifespan > 80 && !this.getEntityWorld().isClient()) this.discard();
    }

    @Override
    protected void onBlockCollision(BlockState state) {
        if (this.getEntityWorld().getBlockState(this.getBlockPos()).isIn(BlockTags.DIRT)) {
            BlockState toCheckUp = this.getEntityWorld().getBlockState(this.getBlockPos().up());
            if (WeedPlacement.canWeedsReplace(toCheckUp)) {
                this.getEntityWorld().setBlockState(this.getBlockPos().up(), ModBlocks.POPPER_PLANT.getDefaultState());
                this.discard();
            }
        } else if (this.getEntityWorld().getBlockState(this.getBlockPos().down()).isIn(BlockTags.DIRT)) {
            BlockState toCheck = this.getEntityWorld().getBlockState(this.getBlockPos());
            if (WeedPlacement.canWeedsReplace(toCheck)) {
                this.getEntityWorld().setBlockState(this.getBlockPos(), ModBlocks.POPPER_PLANT.getDefaultState());
                this.discard();
            }
        }
    }
}
