package yelf42.cropcritters.entity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import yelf42.cropcritters.blocks.ModBlocks;

import java.util.function.Predicate;

public class BeetrootCritterEntity extends AbstractCropCritterEntity {
    public BeetrootCritterEntity(EntityType<? extends TameableEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected Predicate<BlockState> getTargetBlockFilter() {
        return (blockState -> blockState.isOf(Blocks.ROOTED_DIRT));
    }

    @Override
    protected  int getTargetOffset() {return 1;}

    @Override
    public void completeTargetGoal() {
        if (this.targetPos == null) return;
        this.playSound(SoundEvents.ITEM_HOE_TILL, 1.0F, 1.0F);
        this.getWorld().setBlockState(this.targetPos, Blocks.DIRT.getDefaultState(), Block.NOTIFY_ALL_AND_REDRAW);
        ((ServerWorld)this.getWorld()).spawnParticles(ParticleTypes.DUST_PLUME, this.targetPos.getX() + 0.5, this.targetPos.getY() + 1.0, this.targetPos.getZ() + 0.5, 10, 0.5, 0.5, 0.5, 0.0);
    }

    @Override
    protected boolean isHealingItem(ItemStack itemStack) {
        return itemStack.isOf(Items.BEETROOT);
    }

    @Override
    protected int resetTicksUntilCanWork() {
        return MathHelper.nextInt(this.random, 100, 200);
    }
}
