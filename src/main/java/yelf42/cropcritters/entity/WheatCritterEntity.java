package yelf42.cropcritters.entity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Pair;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import yelf42.cropcritters.CropCritters;

import java.util.function.Predicate;

public class WheatCritterEntity extends AbstractCropCritterEntity {
    public WheatCritterEntity(EntityType<? extends TameableEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected Predicate<BlockState> getTargetBlockFilter() {
        return (blockState -> blockState.isIn(CropCritters.WEEDS) || blockState.isOf(Blocks.DEAD_BUSH));
    }

    @Override
    protected  int getTargetOffset() {return 0;}

    @Override
    public void completeTargetGoal() {
        if (this.targetPos == null) return;
        this.playSound(SoundEvents.ITEM_SHEARS_SNIP, 1.0F, 1.0F);
        this.getWorld().syncWorldEvent(this, 2001, this.targetPos, Block.getRawIdFromState(this.getWorld().getBlockState(this.targetPos)));
        this.getWorld().setBlockState(this.targetPos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL_AND_REDRAW);
    }

    @Override
    protected Pair<Item, Integer> getLoot() {
        return new Pair<>(Items.WHEAT, 4);
    }

    @Override
    protected boolean isHealingItem(ItemStack itemStack) {
        return itemStack.isOf(Items.WHEAT) || itemStack.isOf(Items.WHEAT_SEEDS);
    }

    @Override
    protected int resetTicksUntilCanWork() {
        return resetTicksUntilCanWork(MathHelper.nextInt(this.random, 100, 200));
    }
}
