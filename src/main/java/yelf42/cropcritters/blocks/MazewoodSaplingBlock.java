package yelf42.cropcritters.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;
import yelf42.cropcritters.config.ConfigManager;
import yelf42.cropcritters.events.WeedGrowNotifier;

public class MazewoodSaplingBlock extends BlockWithEntity implements Fertilizable {
    public static final MapCodec<MazewoodSaplingBlock> CODEC = createCodec(MazewoodSaplingBlock::new);
    private static final VoxelShape SHAPE = Block.createColumnShape((double)8.0F, (double)0.0F, (double)8.0F);;
    public static final IntProperty SPREAD = IntProperty.of("spread", 0, 128);

    public MazewoodSaplingBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(this.getSpreadProperty(), ConfigManager.CONFIG.mazewoodSpread));
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new MazewoodSaplingBlockEntity(pos, state);
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
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof MazewoodSaplingBlockEntity spreader) {
            spreader.tickScheduled();
        }
    }

    @Override
    protected boolean hasRandomTicks(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof MazewoodSaplingBlockEntity spreader) {
            spreader.tickRandom(random);
        }
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
    protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        return world.getBlockState(pos.down()).isIn(BlockTags.DIRT);
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
        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof MazewoodSaplingBlockEntity spreader) {
            spreader.tickRandom(random);
        }
    }
}
