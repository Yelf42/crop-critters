package yelf42.cropcritters.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;
import yelf42.cropcritters.config.ConfigManager;
import yelf42.cropcritters.events.WeedGrowNotifier;

public class MazewoodSaplingBlock extends PlantBlock implements Fertilizable {
    public static final MapCodec<MazewoodSaplingBlock> CODEC = createCodec(MazewoodSaplingBlock::new);
    private static final VoxelShape SHAPE = Block.createColumnShape((double)8.0F, (double)0.0F, (double)8.0F);;


    public static final long[] MAZE_TILES = new long[] {
            0b1111011110100000101011101000101000001010100010001010101110100000L,
            0b1111011110100000101011101010100000111010100000101111111010000000L,
            0b1111011110100000101111101010001000101010101010001010111010000000L,
            0b1111011110100010101010101000101000001010101000101010111010000000L,
            0b1111011110000000111000101010001000101010101000101011111010000000L,
            0b1111011110000000111001101010001000111010100010101010101010100000L,
            0b1111011110000000111001101010001000111010100010001011111010000000L,
            0b1111011110000000111000101000001000111010100010101110111010000000L,
            0b1111011110000000111000111000000000111010101000101011111010000000L,
            0b1111011110000000111000111000000000111110101000101010101010000000L,
            0b1111011110000000111111101000100000101010101010001011111010000000L,
            0b1111011110000000111111101010000000101110101010001010101010000010L,
            0b1111011110000000111111101010001000101010101010001010111110000000L,
            0b1111011110000010111110101000001000111110100010001110111010000000L,
            0b1111011110000010111110101000100000101110101010001011101110000000L,
            0b1111011110000010111110101000100000111110100000101011101010000000L
    };
    public static final IntProperty SPREAD = IntProperty.of("spread", 0, 128);

    public MazewoodSaplingBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(this.getSpreadProperty(), ConfigManager.CONFIG.mazewoodSpread));
    }

    @Override
    public MapCodec<? extends MazewoodSaplingBlock> getCodec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    protected IntProperty getSpreadProperty() {
        return SPREAD;
    }

    public int getSpread(BlockState state) {
        return (Integer)state.get(this.getSpreadProperty());
    }

    public BlockState withSpread(int spread) {
        return this.getDefaultState().with(this.getSpreadProperty(), spread);
    }

    @Override
    protected boolean hasRandomTicks(BlockState state) {
        return true;
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

        mature(state, world, pos);
    }

    protected void mature(BlockState state, ServerWorld world, BlockPos pos) {
        // Die if not part of the maze
        if (!isWall(pos)) {
            world.setBlockState(pos, Blocks.DEAD_BUSH.getDefaultState());
            world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(null, state));
            return;
        }

        if (this.getSpread(state) > 0) {
            Iterable<BlockPos> iterable = BlockPos.iterateOutwards(pos, 2, 1, 2);
            for(BlockPos blockPos : iterable) {
                if (isWall(blockPos)) {
                    BlockState checkState = world.getBlockState(blockPos);
                    BlockState checkBelowState = world.getBlockState(blockPos.down());
                    if (canPlantOnTop(checkBelowState, world, blockPos.down()) && (checkState.isAir() || (!(checkState.getBlock() instanceof SpreadingWeekBlock) && !(checkState.getBlock() instanceof MazewoodSaplingBlock) && (checkState.getBlock() instanceof PlantBlock)))) {
                        BlockState blockState = this.withSpread(this.getSpread(state) - 1);
                        world.setBlockState(blockPos, blockState);
                        world.emitGameEvent(GameEvent.BLOCK_CHANGE, blockPos, GameEvent.Emitter.of(null, blockState));
                    }
                }
            }
        }

        BlockState matureMazewood = ModBlocks.MAZEWOOD.getDefaultState();

        world.setBlockState(pos, matureMazewood);
        world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(null, state));

        if (world.getBlockState(pos.up()).isAir()) {
            world.setBlockState(pos.up(), matureMazewood);
            world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos.up(), GameEvent.Emitter.of(null, state));

            if (world.getBlockState(pos.up().up()).isAir()) {
                world.setBlockState(pos.up().up(), matureMazewood);
                world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos.up().up(), GameEvent.Emitter.of(null, state));
            }
        }
    }

    public static boolean isWall(BlockPos blockPos) {
        int tileSize = 8;

        int tileX = Math.floorDiv(blockPos.getX(), tileSize);
        int tileZ = Math.floorDiv(blockPos.getZ(), tileSize);
        int mazeTile = Math.floorMod((int)(((long)tileX * 7342871L) ^ ((long)tileZ * 912783L)), 16);

        int localX = Math.floorMod(blockPos.getX(), tileSize);
        int localZ = Math.floorMod(blockPos.getZ(), tileSize);
        int bitIndex = localZ * tileSize + localX;

        return ((MAZE_TILES[mazeTile] >> bitIndex) & 1L) != 0;
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
    }

    @Override
    protected void onStateReplaced(BlockState state, ServerWorld world, BlockPos pos, boolean moved) {
        WeedGrowNotifier.notifyRemoval(world, pos);
        super.onStateReplaced(state, world, pos, moved);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(SPREAD);
    }

    @Override
    public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
        return (double)world.random.nextFloat() < 0.45;
    }

    @Override
    public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
        mature(state, world, pos);
    }
}
