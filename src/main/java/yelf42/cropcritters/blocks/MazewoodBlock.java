package yelf42.cropcritters.blocks;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.*;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.tick.ScheduledTickView;

import java.util.Map;
import java.util.function.Function;

public class MazewoodBlock extends Block {
    public static final MapCodec<MazewoodBlock> CODEC = createCodec(MazewoodBlock::new);
    public static final BooleanProperty UP;
    public static final EnumProperty<MazewoodShape> EAST_WALL_SHAPE;
    public static final EnumProperty<MazewoodShape> NORTH_WALL_SHAPE;
    public static final EnumProperty<MazewoodShape> SOUTH_WALL_SHAPE;
    public static final EnumProperty<MazewoodShape> WEST_WALL_SHAPE;
    public static final Map<Direction, EnumProperty<MazewoodShape>> WALL_SHAPE_PROPERTIES_BY_DIRECTION;
    private final Function<BlockState, VoxelShape> outlineShapeFunction;
    private final Function<BlockState, VoxelShape> collisionShapeFunction;
    private static final Map<Direction, VoxelShape> WALL_SHAPES_FOR_TALL_TEST_BY_DIRECTION;

    public MapCodec<MazewoodBlock> getCodec() {
        return CODEC;
    }

    public MazewoodBlock(AbstractBlock.Settings settings) {
        super(settings);
        this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(UP, true)).with(NORTH_WALL_SHAPE, MazewoodShape.NONE)).with(EAST_WALL_SHAPE, MazewoodShape.NONE)).with(SOUTH_WALL_SHAPE, MazewoodShape.NONE)).with(WEST_WALL_SHAPE, MazewoodShape.NONE)));
        this.outlineShapeFunction = this.createShapeFunction(16.0F);
        this.collisionShapeFunction = this.createShapeFunction(24.0F);
    }

    private Function<BlockState, VoxelShape> createShapeFunction(float tallHeight) {
        VoxelShape voxelShape = Block.createColumnShape((double)8.0F, (double)0.0F, (double)tallHeight);
        Map<Direction, VoxelShape> map = VoxelShapes.createHorizontalFacingShapeMap(Block.createCuboidZShape((double)8.0F, (double)0.0F, (double)tallHeight, (double)0.0F, (double)11.0F));
        return this.createShapeFunction((state) -> {
            VoxelShape voxelShape2 = (Boolean)state.get(UP) ? voxelShape : VoxelShapes.empty();

            for(Map.Entry<Direction, EnumProperty<MazewoodShape>> entry : WALL_SHAPE_PROPERTIES_BY_DIRECTION.entrySet()) {
                VoxelShape var10001;
                switch ((MazewoodShape)state.get((Property)entry.getValue())) {
                    case NONE -> var10001 = VoxelShapes.empty();
                    case TALL -> var10001 = (VoxelShape)map.get(entry.getKey());
                    default -> throw new MatchException((String)null, (Throwable)null);
                }

                voxelShape2 = VoxelShapes.union(voxelShape2, var10001);
            }

            return voxelShape2;
        });
    }

    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return (VoxelShape)this.outlineShapeFunction.apply(state);
    }

    protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return (VoxelShape)this.collisionShapeFunction.apply(state);
    }

    protected boolean canPathfindThrough(BlockState state, NavigationType type) {
        return false;
    }

    private boolean shouldConnectTo(BlockState state, boolean faceFullSquare, Direction side) {
        return (state.getBlock() instanceof MazewoodBlock) || !cannotConnect(state) && faceFullSquare;
    }

    public BlockState getPlacementState(ItemPlacementContext ctx) {
        WorldView worldView = ctx.getWorld();
        BlockPos blockPos = ctx.getBlockPos();
        BlockPos blockPos2 = blockPos.north();
        BlockPos blockPos3 = blockPos.east();
        BlockPos blockPos4 = blockPos.south();
        BlockPos blockPos5 = blockPos.west();
        BlockPos blockPos6 = blockPos.up();
        BlockState blockState = worldView.getBlockState(blockPos2);
        BlockState blockState2 = worldView.getBlockState(blockPos3);
        BlockState blockState3 = worldView.getBlockState(blockPos4);
        BlockState blockState4 = worldView.getBlockState(blockPos5);
        BlockState blockState5 = worldView.getBlockState(blockPos6);
        boolean bl = this.shouldConnectTo(blockState, blockState.isSideSolidFullSquare(worldView, blockPos2, Direction.SOUTH), Direction.SOUTH);
        boolean bl2 = this.shouldConnectTo(blockState2, blockState2.isSideSolidFullSquare(worldView, blockPos3, Direction.WEST), Direction.WEST);
        boolean bl3 = this.shouldConnectTo(blockState3, blockState3.isSideSolidFullSquare(worldView, blockPos4, Direction.NORTH), Direction.NORTH);
        boolean bl4 = this.shouldConnectTo(blockState4, blockState4.isSideSolidFullSquare(worldView, blockPos5, Direction.EAST), Direction.EAST);
        BlockState blockState6 = (BlockState)this.getDefaultState();
        return this.getStateWith(worldView, blockState6, blockPos6, blockState5, bl, bl2, bl3, bl4);
    }

    protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
        if (!this.canPlaceAt(state, world, pos)) {
            if (world instanceof World w) {
                w.scheduleBlockTick(pos, this, 1);
            }
            return Blocks.AIR.getDefaultState();
        }

        if (direction == Direction.DOWN) {
            return super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);
        } else {
            return direction == Direction.UP ? this.getStateAt(world, state, neighborPos, neighborState) : this.getStateWithNeighbor(world, pos, state, neighborPos, neighborState, direction);
        }
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        world.setBlockState(pos, Blocks.AIR.getDefaultState());
        world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(state));
    }

    @Override
    protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        BlockState floor = world.getBlockState(pos.down());
        return floor.isIn(BlockTags.DIRT) || floor.isOf(Blocks.FARMLAND) || (floor.getBlock() instanceof MazewoodBlock);
    }

    private static boolean isConnected(BlockState state, Property<MazewoodShape> property) {
        return state.get(property) != MazewoodShape.NONE;
    }

    private BlockState getStateAt(WorldView world, BlockState state, BlockPos pos, BlockState aboveState) {
        boolean bl = isConnected(state, NORTH_WALL_SHAPE);
        boolean bl2 = isConnected(state, EAST_WALL_SHAPE);
        boolean bl3 = isConnected(state, SOUTH_WALL_SHAPE);
        boolean bl4 = isConnected(state, WEST_WALL_SHAPE);
        return this.getStateWith(world, state, pos, aboveState, bl, bl2, bl3, bl4);
    }

    private BlockState getStateWithNeighbor(WorldView world, BlockPos pos, BlockState state, BlockPos neighborPos, BlockState neighborState, Direction direction) {
        Direction direction2 = direction.getOpposite();
        boolean bl = direction == Direction.NORTH ? this.shouldConnectTo(neighborState, neighborState.isSideSolidFullSquare(world, neighborPos, direction2), direction2) : isConnected(state, NORTH_WALL_SHAPE);
        boolean bl2 = direction == Direction.EAST ? this.shouldConnectTo(neighborState, neighborState.isSideSolidFullSquare(world, neighborPos, direction2), direction2) : isConnected(state, EAST_WALL_SHAPE);
        boolean bl3 = direction == Direction.SOUTH ? this.shouldConnectTo(neighborState, neighborState.isSideSolidFullSquare(world, neighborPos, direction2), direction2) : isConnected(state, SOUTH_WALL_SHAPE);
        boolean bl4 = direction == Direction.WEST ? this.shouldConnectTo(neighborState, neighborState.isSideSolidFullSquare(world, neighborPos, direction2), direction2) : isConnected(state, WEST_WALL_SHAPE);
        BlockPos blockPos = pos.up();
        BlockState blockState = world.getBlockState(blockPos);
        return this.getStateWith(world, state, blockPos, blockState, bl, bl2, bl3, bl4);
    }

    private BlockState getStateWith(WorldView world, BlockState state, BlockPos pos, BlockState aboveState, boolean north, boolean east, boolean south, boolean west) {
        VoxelShape voxelShape = aboveState.getCollisionShape(world, pos).getFace(Direction.DOWN);
        BlockState blockState = this.getStateWith(state, north, east, south, west, voxelShape);
        return (BlockState)blockState.with(UP, true);
    }

    private BlockState getStateWith(BlockState state, boolean north, boolean east, boolean south, boolean west, VoxelShape aboveShape) {
        return (BlockState)((BlockState)((BlockState)((BlockState)state.with(NORTH_WALL_SHAPE, this.getMazewoodShape(north, aboveShape, (VoxelShape)WALL_SHAPES_FOR_TALL_TEST_BY_DIRECTION.get(Direction.NORTH)))).with(EAST_WALL_SHAPE, this.getMazewoodShape(east, aboveShape, (VoxelShape)WALL_SHAPES_FOR_TALL_TEST_BY_DIRECTION.get(Direction.EAST)))).with(SOUTH_WALL_SHAPE, this.getMazewoodShape(south, aboveShape, (VoxelShape)WALL_SHAPES_FOR_TALL_TEST_BY_DIRECTION.get(Direction.SOUTH)))).with(WEST_WALL_SHAPE, this.getMazewoodShape(west, aboveShape, (VoxelShape)WALL_SHAPES_FOR_TALL_TEST_BY_DIRECTION.get(Direction.WEST)));
    }

    private MazewoodShape getMazewoodShape(boolean connected, VoxelShape aboveShape, VoxelShape tallShape) {
        if (connected) {
            return MazewoodShape.TALL;
        } else {
            return MazewoodShape.NONE;
        }
    }

    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(new Property[]{UP, NORTH_WALL_SHAPE, EAST_WALL_SHAPE, WEST_WALL_SHAPE, SOUTH_WALL_SHAPE});
    }

    protected BlockState rotate(BlockState state, BlockRotation rotation) {
        switch (rotation) {
            case CLOCKWISE_180 -> {
                return (BlockState)((BlockState)((BlockState)((BlockState)state.with(NORTH_WALL_SHAPE, (MazewoodShape)state.get(SOUTH_WALL_SHAPE))).with(EAST_WALL_SHAPE, (MazewoodShape)state.get(WEST_WALL_SHAPE))).with(SOUTH_WALL_SHAPE, (MazewoodShape)state.get(NORTH_WALL_SHAPE))).with(WEST_WALL_SHAPE, (MazewoodShape)state.get(EAST_WALL_SHAPE));
            }
            case COUNTERCLOCKWISE_90 -> {
                return (BlockState)((BlockState)((BlockState)((BlockState)state.with(NORTH_WALL_SHAPE, (MazewoodShape)state.get(EAST_WALL_SHAPE))).with(EAST_WALL_SHAPE, (MazewoodShape)state.get(SOUTH_WALL_SHAPE))).with(SOUTH_WALL_SHAPE, (MazewoodShape)state.get(WEST_WALL_SHAPE))).with(WEST_WALL_SHAPE, (MazewoodShape)state.get(NORTH_WALL_SHAPE));
            }
            case CLOCKWISE_90 -> {
                return (BlockState)((BlockState)((BlockState)((BlockState)state.with(NORTH_WALL_SHAPE, (MazewoodShape)state.get(WEST_WALL_SHAPE))).with(EAST_WALL_SHAPE, (MazewoodShape)state.get(NORTH_WALL_SHAPE))).with(SOUTH_WALL_SHAPE, (MazewoodShape)state.get(EAST_WALL_SHAPE))).with(WEST_WALL_SHAPE, (MazewoodShape)state.get(SOUTH_WALL_SHAPE));
            }
            default -> {
                return state;
            }
        }
    }

    protected BlockState mirror(BlockState state, BlockMirror mirror) {
        switch (mirror) {
            case LEFT_RIGHT -> {
                return (BlockState)((BlockState)state.with(NORTH_WALL_SHAPE, (MazewoodShape)state.get(SOUTH_WALL_SHAPE))).with(SOUTH_WALL_SHAPE, (MazewoodShape)state.get(NORTH_WALL_SHAPE));
            }
            case FRONT_BACK -> {
                return (BlockState)((BlockState)state.with(EAST_WALL_SHAPE, (MazewoodShape)state.get(WEST_WALL_SHAPE))).with(WEST_WALL_SHAPE, (MazewoodShape)state.get(EAST_WALL_SHAPE));
            }
            default -> {
                return super.mirror(state, mirror);
            }
        }
    }

    static {
        UP = Properties.UP;
        EAST_WALL_SHAPE = EnumProperty.of("east", MazewoodShape.class);
        NORTH_WALL_SHAPE = EnumProperty.of("north", MazewoodShape.class);
        SOUTH_WALL_SHAPE = EnumProperty.of("south", MazewoodShape.class);
        WEST_WALL_SHAPE = EnumProperty.of("west", MazewoodShape.class);
        WALL_SHAPE_PROPERTIES_BY_DIRECTION = ImmutableMap.copyOf(Maps.newEnumMap(Map.of(Direction.NORTH, NORTH_WALL_SHAPE, Direction.EAST, EAST_WALL_SHAPE, Direction.SOUTH, SOUTH_WALL_SHAPE, Direction.WEST, WEST_WALL_SHAPE)));
        WALL_SHAPES_FOR_TALL_TEST_BY_DIRECTION = VoxelShapes.createHorizontalFacingShapeMap(Block.createCuboidZShape((double)2.0F, (double)16.0F, (double)0.0F, (double)9.0F));
    }
}