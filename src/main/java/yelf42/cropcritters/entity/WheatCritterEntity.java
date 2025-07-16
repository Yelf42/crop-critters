package yelf42.cropcritters.entity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import yelf42.cropcritters.CropCritters;

import java.util.function.Predicate;

public class WheatCritterEntity extends AbstractCropCritterEntity {
    public WheatCritterEntity(EntityType<? extends TameableEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected Predicate<BlockState> getTargetBlockFilter() {
        return (blockState -> blockState.isIn(CropCritters.WEEDS));
    }

    @Override
    protected  int getTargetOffset() {return 0;}

    @Override
    protected BlockState getTargetBlockChanged(@Nullable Block target) {return Blocks.AIR.getDefaultState();}

    @Override
    protected  Item getHealingItem() {return Items.WHEAT;}

    @Override
    protected int resetTicksUntilCanWork() {
        return MathHelper.nextInt(this.random, 100, 200);
    }
}
