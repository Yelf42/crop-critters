package yelf42.cropcritters.entity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Pair;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import yelf42.cropcritters.sound.ModSounds;

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
        this.playSound(ModSounds.ENTITY_CRITTER_TILL, 1.0F, 1.0F);
        this.getEntityWorld().setBlockState(this.targetPos, Blocks.DIRT.getDefaultState(), Block.NOTIFY_ALL_AND_REDRAW);
        this.getEntityWorld().syncWorldEvent(this, 2001, this.targetPos, Block.getRawIdFromState(this.getEntityWorld().getBlockState(this.targetPos)));
    }

    @Override
    protected Pair<Item, Integer> getLoot() {
        return new Pair<>(Items.BEETROOT, 5);
    }

    @Override
    protected boolean isHealingItem(ItemStack itemStack) {
        return itemStack.isOf(Items.BEETROOT) || itemStack.isOf(Items.BEETROOT_SEEDS);
    }

    @Override
    protected int resetTicksUntilCanWork() {
        return resetTicksUntilCanWork(MathHelper.nextInt(this.random, 100, 200));
    }
}
