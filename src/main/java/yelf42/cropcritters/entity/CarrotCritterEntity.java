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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import yelf42.cropcritters.blocks.ModBlocks;
import java.util.function.Predicate;

import static net.minecraft.block.Block.pushEntitiesUpBeforeBlockChange;

public class CarrotCritterEntity extends AbstractCropCritterEntity {
    public CarrotCritterEntity(EntityType<? extends TameableEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected Predicate<BlockState> getTargetBlockFilter() {
        return (blockState -> blockState.isOf(Blocks.DIRT) || blockState.isOf(Blocks.GRASS_BLOCK)
                                        || blockState.isOf(Blocks.SOUL_SOIL) || blockState.isOf(Blocks.SOUL_SAND));
    }

    @Override
    protected  int getTargetOffset() {return 1;}

    @Override
    public void completeTargetGoal() {
        if (this.targetPos == null) return;
        this.playSound(SoundEvents.ITEM_HOE_TILL, 1.0F, 1.0F);
        BlockState target = this.getWorld().getBlockState(this.targetPos);
        BlockState farmland = (target.isOf(Blocks.DIRT) || target.isOf(Blocks.GRASS_BLOCK)) ? Blocks.FARMLAND.getDefaultState() : (target.isOf(Blocks.SOUL_SAND) || target.isOf(Blocks.SOUL_SOIL)) ? ModBlocks.SOUL_FARMLAND.getDefaultState() : null;
        if (farmland == null) return;
        this.getWorld().setBlockState(this.targetPos, farmland, Block.NOTIFY_ALL_AND_REDRAW);
        this.getWorld().syncWorldEvent(this, 2001, this.targetPos, Block.getRawIdFromState(this.getWorld().getBlockState(this.targetPos)));
    }

    @Override
    protected Pair<Item, Integer> getLoot() {
        return new Pair<>(Items.CARROT, 6);
    }

    @Override
    protected boolean isHealingItem(ItemStack itemStack) {
        return itemStack.isOf(Items.CARROT) || itemStack.isOf(Items.GOLDEN_CARROT);
    }

    @Override
    protected int resetTicksUntilCanWork() {
        return resetTicksUntilCanWork(MathHelper.nextInt(this.random, 100, 200));
    }

    @Override
    public boolean isAttractive(BlockPos pos) {
        BlockState target = this.getWorld().getBlockState(pos);
        BlockState above = this.getWorld().getBlockState(pos.up());
        return this.getTargetBlockFilter().test(target) && above.getCollisionShape(null, pos.up()).isEmpty();
    }
}
