package yelf42.cropcritters.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.tick.ScheduledTickView;
import org.jetbrains.annotations.Nullable;
import yelf42.cropcritters.events.WeedGrowNotifier;

public class SpreadingWeekBlock extends PlantBlock {
    public static final MapCodec<SpreadingWeekBlock> CODEC = createCodec(SpreadingWeekBlock::new);
    public static final int MAX_AGE = 1;
    public static final IntProperty AGE = Properties.AGE_1;
    private static final VoxelShape[] SHAPES_BY_AGE = Block.createShapeArray(2, age -> Block.createColumnShape(16.0, 0.0, 8 + age * 8));
    private boolean reachedMaxNeighbours = false;

    public SpreadingWeekBlock(AbstractBlock.Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(this.getAgeProperty(), 0));
    }

    @Override
    public MapCodec<? extends SpreadingWeekBlock> getCodec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPES_BY_AGE[this.getAge(state)];
    }

    public int getMaxNeighbours() { return 0; }

    protected IntProperty getAgeProperty() {
        return AGE;
    }

    public int getMaxAge() {
        return MAX_AGE;
    }

    public int getAge(BlockState state) {
        return (Integer)state.get(this.getAgeProperty());
    }

    public BlockState withAge(int age) {
        return this.getDefaultState().with(this.getAgeProperty(), age);
    }

    public final boolean isMature(BlockState state) {
        return this.getAge(state) >= this.getMaxAge();
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
        reachedMaxNeighbours = false;
        return super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    protected boolean hasRandomTicks(BlockState state) {
        return !reachedMaxNeighbours;
    }

    @Override
    protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (this.isMature(state)) {
            // Count neighbouring weeds
            int neighbouringWeeds = -1;
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    BlockPos checkPos = pos.add(i, 0, j);
                    BlockState checkState = world.getBlockState(checkPos);
                    if (checkState.isOf(this)) neighbouringWeeds++;
                }
            }

            // Place new weed if mature and <2 neighbouring weeds and target is plantable
            if (neighbouringWeeds < getMaxNeighbours()) {
                int i = random.nextInt(3) - 1;
                int j = (i == 0) ? 1 + -2 * random.nextInt(2) : 0;
                BlockPos targetPos = pos.add(i, -1, j);
                BlockState targetState = world.getBlockState(targetPos);
                BlockState aboveTargetState = world.getBlockState(targetPos.up());
                if (canPlantOnTop(targetState, world, targetPos) && (aboveTargetState.isAir() || aboveTargetState.isIn(BlockTags.MAINTAINS_FARMLAND) || aboveTargetState.isIn(BlockTags.FLOWERS))) {
                    setToWeed(world, targetPos.up());
                }
            } else {
                reachedMaxNeighbours = true;
            }
        } else if (random.nextInt(2) == 0) {
            // 50% chance to mature per random tick
            world.setBlockState(pos, this.withAge(this.getMaxAge()), Block.NOTIFY_LISTENERS);
        }
    }

    public void setToWeed(World world, BlockPos pos) {
        BlockState blockState = this.getDefaultState();
        world.setBlockState(pos, blockState);
        WeedGrowNotifier.notifyEvent(world, pos);
        world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(null, blockState));
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        WeedGrowNotifier.notifyEvent(world, pos);
        super.onPlaced(world, pos, state, placer, itemStack);
    }

    @Override
    protected void onStateReplaced(BlockState state, ServerWorld world, BlockPos pos, boolean moved) {
        WeedGrowNotifier.notifyRemoval(world, pos);
        super.onStateReplaced(state, world, pos, moved);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

}
