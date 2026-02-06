package yelf42.cropcritters.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;
import org.jspecify.annotations.Nullable;

public class SoulRoseBlock extends BlockWithEntity {
    public static final MapCodec<SoulRoseBlock> CODEC = createCodec(SoulRoseBlock::new);
    public static final EnumProperty<DoubleBlockHalf> HALF = Properties.DOUBLE_BLOCK_HALF;
    public static final IntProperty LEVEL = IntProperty.of("level", 0, 3);
    public static final EnumProperty<SoulRoseType> TYPE = EnumProperty.of("type", SoulRoseType.class);

    private static final VoxelShape SMALL_0_SHAPE = Block.createColumnShape((double)6.0F, (double)0.0F, (double)11.0F);
    private static final VoxelShape SMALL_1_SHAPE = Block.createColumnShape((double)10.0F, (double)0.0F, (double)16.0F);
    private static final VoxelShape LARGE_SHAPE = Block.createColumnShape((double)14.0F, (double)0.0F, (double)16.0F);

    protected SoulRoseBlock(Settings settings) {
        super(settings);
        this.setDefaultState((this.stateManager.getDefaultState()).with(HALF, DoubleBlockHalf.LOWER).with(TYPE, SoulRoseType.NONE));
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        if (state.get(HALF, DoubleBlockHalf.UPPER) == DoubleBlockHalf.LOWER) {
            return new SoulRoseBlockEntity(pos, state);
        }
        return null;
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return validateTicker(type, ModBlockEntities.SOUL_ROSE, world.isClient() ? SoulRoseBlockEntity::clientTick : SoulRoseBlockEntity::serverTick);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return switch(state.get(LEVEL, 0)) {
            case 0 -> SMALL_0_SHAPE;
            case 1 -> SMALL_1_SHAPE;
            default -> LARGE_SHAPE;
        };
    }

    public static boolean isDoubleTallAtLevel(int level) {
        return level >= 2;
    }

    @Override
    public @Nullable BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockPos blockPos = ctx.getBlockPos();
        World world = ctx.getWorld();
        return blockPos.getY() < world.getTopYInclusive() && world.getBlockState(blockPos.up()).canReplace(ctx) ? super.getPlacementState(ctx) : null;
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
        if (isDoubleTallAtLevel(state.get(LEVEL))) {
            DoubleBlockHalf doubleBlockHalf = state.get(HALF);
            if (direction.getAxis() != Direction.Axis.Y || doubleBlockHalf == DoubleBlockHalf.LOWER != (direction == Direction.UP) || neighborState.isOf(this) && neighborState.get(HALF) != doubleBlockHalf) {
                return doubleBlockHalf == DoubleBlockHalf.LOWER && direction == Direction.DOWN && !state.canPlaceAt(world, pos) ? Blocks.AIR.getDefaultState() : super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);
            } else {
                return Blocks.AIR.getDefaultState();
            }
        } else {
            return state.canPlaceAt(world, pos) ? state : Blocks.AIR.getDefaultState();
        }
    }

    @Override
    protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        BlockState below = world.getBlockState(pos.down());
        if (state.get(HALF) != DoubleBlockHalf.UPPER) {
            return below.isOf(Blocks.SOUL_SOIL) || below.isOf(Blocks.SOUL_SAND);
        } else {
            return below.isOf(this) && below.get(HALF) == DoubleBlockHalf.LOWER;
        }
    }

    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(HALF);
        builder.add(LEVEL);
        builder.add(TYPE);
    }
}
