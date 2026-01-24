package yelf42.cropcritters.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
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
import yelf42.cropcritters.CropCritters;
import yelf42.cropcritters.config.WeedPlacement;
import yelf42.cropcritters.events.WeedGrowNotifier;

import java.util.ArrayList;
import java.util.List;


public class SpreadingWeedBlock extends PlantBlock implements Fertilizable{
    public static final MapCodec<SpreadingWeedBlock> CODEC = createCodec(SpreadingWeedBlock::new);
    public static final int MAX_AGE = 1;
    public static final IntProperty AGE = Properties.AGE_1;
    private static final VoxelShape[] SHAPES_BY_AGE = Block.createShapeArray(2, age -> Block.createColumnShape(8 + age * 4, 0.0, 8 + age * 4));
    private boolean reachedMaxNeighbours = false;

    public SpreadingWeedBlock(AbstractBlock.Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(AGE, 0));
    }

    @Override
    public MapCodec<? extends SpreadingWeedBlock> getCodec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPES_BY_AGE[this.getAge(state)];
    }

    public int getMaxNeighbours() { return 0; }

    public int getMaxAge() {
        return MAX_AGE;
    }

    public int getAge(BlockState state) {
        return (Integer)state.get(AGE);
    }

    public BlockState withAge(int age) {
        return this.getDefaultState().with(AGE, age);
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

        // Turn farmlands bad
        BlockState soilCheck = world.getBlockState(pos.down());
        if (soilCheck.isOf(Blocks.FARMLAND)) {
            BlockState toDirt = (random.nextInt(4) == 0) ? Blocks.DIRT.getDefaultState() : (random.nextInt(2) == 0) ? Blocks.ROOTED_DIRT.getDefaultState() : Blocks.COARSE_DIRT.getDefaultState();
            world.setBlockState(pos.down(), toDirt, Block.NOTIFY_LISTENERS);
        } else if (soilCheck.isOf(ModBlocks.SOUL_FARMLAND)){
            BlockState toDirt = (random.nextInt(2) == 0) ? Blocks.SOUL_SOIL.getDefaultState() : Blocks.SOUL_SAND.getDefaultState();
            world.setBlockState(pos.down(), toDirt, Block.NOTIFY_LISTENERS);
        }

        if (this.isMature(state)) {
            // Count neighbouring weeds and finding spots to spread to
            List<BlockPos> canSpreadTo = new ArrayList<>();
            int neighbouringWeeds = -1;
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    for (int k = -1; k <= 1; k++) {
                        BlockPos checkPos = pos.add(i, k, j);
                        BlockState checkState = world.getBlockState(checkPos);
                        if (checkState.isOf(this)) neighbouringWeeds++;
                        BlockState checkBelowState = world.getBlockState(checkPos.down());
                        if (canPlantOnTop(checkBelowState, world, checkPos.down()) && WeedPlacement.canWeedsReplace(checkState)) {
                            canSpreadTo.add(checkPos);
                        }
                    }
                }
            }

            // Place new weed if mature and <2 neighbouring weeds and target is plantable
            if (neighbouringWeeds < getMaxNeighbours() && !canSpreadTo.isEmpty()) {
                BlockPos targetPos = canSpreadTo.get(random.nextInt(canSpreadTo.size()));
                setToWeed(world, targetPos);
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
    protected void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        WeedGrowNotifier.notifyEvent(world, pos);
        super.onBlockAdded(state, world, pos, oldState, notify);
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

    @Override
    public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state) {
        return !isMature(state) || Fertilizable.canSpread(world, pos, state);
    }

    @Override
    public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
        return random.nextInt(2) == 0;
    }

    @Override
    public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
        if (!isMature(state)) {
            world.setBlockState(pos, this.withAge(this.getMaxAge()), Block.NOTIFY_LISTENERS);
        } else {
            Fertilizable.findPosToSpreadTo(world, pos, state).ifPresent((posx) -> setToWeed(world,posx));
        }
    }
}
