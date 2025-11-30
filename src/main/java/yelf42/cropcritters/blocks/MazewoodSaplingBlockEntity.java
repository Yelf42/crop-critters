package yelf42.cropcritters.blocks;

import com.mojang.serialization.Codec;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PlantBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.tick.TickPriority;

import java.util.ArrayDeque;

public class MazewoodSaplingBlockEntity extends BlockEntity {

    private final ArrayDeque<Long> growInto = new ArrayDeque<>();
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

    public MazewoodSaplingBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MAZEWOOD_SAPLING, pos, state);
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);

        WriteView.ListAppender<Long> appender = view.getListAppender("GrowInto", Codec.LONG);
        for (Long pos : growInto) {
            appender.add(pos);
        }
    }

    @Override
    protected void readData(ReadView view) {
        super.readData(view);

        growInto.clear();
        ReadView.TypedListReadView<Long> listView = view.getTypedListView("GrowInto", Codec.LONG);
        for (Long posLong : listView) {
            growInto.add(posLong);
        }
    }

    public void tickScheduled() {
        if (world == null || world.isClient()) return;

        if (growInto.isEmpty()) {
            BlockState matureMazewood = ModBlocks.MAZEWOOD.getDefaultState();
            world.playSound(null, pos, SoundEvents.BLOCK_BAMBOO_PLACE, SoundCategory.BLOCKS);

            world.setBlockState(pos, matureMazewood);
            world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(null, matureMazewood));

            if (world.getBlockState(pos.up()).isAir()) {
                world.setBlockState(pos.up(), matureMazewood);
                world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos.up(), GameEvent.Emitter.of(null, matureMazewood));

                if (world.getBlockState(pos.up().up()).isAir()) {
                    world.setBlockState(pos.up().up(), matureMazewood);
                    world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos.up().up(), GameEvent.Emitter.of(null, matureMazewood));
                }
            }
            return;
        }

        BlockPos growPos = BlockPos.fromLong(growInto.removeFirst());
        BlockState checkState = world.getBlockState(growPos);
        BlockState checkBelowState = world.getBlockState(growPos.down());
        boolean planted = false;
        if (canPlantAt(checkState, checkBelowState)) {
            world.playSound(null, growPos, SoundEvents.BLOCK_MANGROVE_ROOTS_STEP, SoundCategory.BLOCKS);
            BlockState blockState = getCachedState().with(MazewoodSaplingBlock.SPREAD, getCachedState().get(MazewoodSaplingBlock.SPREAD) - 1);
            world.setBlockState(growPos, blockState);
            world.emitGameEvent(GameEvent.BLOCK_CHANGE, growPos, GameEvent.Emitter.of(null, blockState));
            planted = true;
        }

        world.scheduleBlockTick(pos, getCachedState().getBlock(), planted ? 15 + world.random.nextInt(30) : 1, TickPriority.EXTREMELY_LOW);
    }

    private boolean canPlantAt(BlockState checkState, BlockState checkBelowState) {
        return (checkBelowState.isIn(BlockTags.DIRT))
                && (checkState.isAir()
                || (!(checkState.getBlock() instanceof MazewoodSaplingBlock)
                    && (checkState.getBlock() instanceof PlantBlock)));
    }

    public void tickRandom(Random random) {
        if (world == null || world.isClient() || !this.growInto.isEmpty()) return;

        BlockState soil = world.getBlockState(pos.down());
        if (soil.isOf(Blocks.FARMLAND)) {
            BlockState to = switch (random.nextInt(3)) {
                case 0 -> Blocks.DIRT.getDefaultState();
                case 1 -> Blocks.ROOTED_DIRT.getDefaultState();
                default -> Blocks.COARSE_DIRT.getDefaultState();
            };
            world.setBlockState(pos.down(), to);
        } else if (soil.isOf(ModBlocks.SOUL_FARMLAND)) {
            world.setBlockState(pos.down(), random.nextBoolean()
                    ? Blocks.SOUL_SOIL.getDefaultState()
                    : Blocks.SOUL_SAND.getDefaultState());
        }

        mature();
    }

    private int getSpread() {
        return getCachedState().get(MazewoodSaplingBlock.SPREAD);
    }

    private void mature() {
        if (world == null || world.isClient()) return;
        if (!isWall(pos)) {
            world.setBlockState(pos, Blocks.DEAD_BUSH.getDefaultState());
            return;
        }

        if (getSpread() > 0) {
            for (BlockPos p : BlockPos.iterateOutwards(pos, 2, 1, 2)) {
                if (isWall(p)) growInto.addLast(p.asLong());
            }
            world.scheduleBlockTick(pos, getCachedState().getBlock(), 15, TickPriority.EXTREMELY_LOW);
        } else {
            world.scheduleBlockTick(pos, getCachedState().getBlock(), 45, TickPriority.EXTREMELY_LOW);
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
}
