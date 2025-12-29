package yelf42.cropcritters.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCollisionHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemPlacementContext;
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
import net.minecraft.world.tick.ScheduledTickView;
import org.jspecify.annotations.Nullable;
import yelf42.cropcritters.CropCritters;
import yelf42.cropcritters.effects.ModEffects;
import yelf42.cropcritters.events.WeedGrowNotifier;

// TODO item texture, model must clip 1 pixel down for farmland

public class StrangleFern extends BlockWithEntity implements Fertilizable {

    public static final MapCodec<StrangleFern> CODEC = createCodec(StrangleFern::new);
    public static final int MAX_AGE = 3;
    public static final IntProperty AGE = Properties.AGE_3;

    public StrangleFern(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(this.getAgeProperty(), 0));
    }

    @Override
    public MapCodec<? extends StrangleFern> getCodec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return Block.createColumnShape(12, 0.0, 4 * Math.min(this.getAge(state), 2) + 4);
    }

    protected IntProperty getAgeProperty() {
        return AGE;
    }

    public int getMaxAge() {
        return MAX_AGE;
    }

    public int getAge(BlockState state) {
        return (Integer)state.get(this.getAgeProperty());
    }

    public final boolean isMature(BlockState state) {
        return this.getAge(state) >= this.getMaxAge();
    }

    public static boolean canInfest(BlockState toCheck) {
        boolean tall = toCheck.contains(Properties.DOUBLE_BLOCK_HALF);
        boolean crop = toCheck.getBlock() instanceof CropBlock;
        boolean canSpread = toCheck.isIn(CropCritters.SPORES_INFECT);
        boolean weed = toCheck.isIn(CropCritters.WEEDS);
        return ((!tall && !weed) && (crop || canSpread));
    }

    @Override
    protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        BlockState floor = world.getBlockState(pos.down());
        return floor.isIn(BlockTags.DIRT);
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
        return !state.canPlaceAt(world, pos) ? Blocks.AIR.getDefaultState() : super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    public @Nullable BlockState getPlacementState(ItemPlacementContext ctx) {
        if (canInfest(ctx.getWorld().getBlockState(ctx.getBlockPos()))) {
            return super.getPlacementState(ctx);
        }
        return null;
    }

    @Override
    protected boolean hasRandomTicks(BlockState state) {
        return !isMature(state);
    }

    @Override
    protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (!isMature(state) && random.nextInt(2) == 0) {
            ageUp(state,world,pos);
        }
    }

    // Increase age, kill host if matured (maybe replace dead_bush with smth smaller)
    private void ageUp(BlockState state, ServerWorld world, BlockPos pos) {
        int newAge = this.getAge(state) + 1;
        world.setBlockState(pos, state.with(AGE, newAge), 3);
        if (newAge == this.getMaxAge()) {
            StrangleFernBlockEntity sfbe = (StrangleFernBlockEntity) world.getBlockEntity(pos);
            if (sfbe != null) sfbe.setInfestedState(Blocks.DEAD_BUSH.getDefaultState());
        }
    }

    @Override
    protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity, EntityCollisionHandler handler, boolean bl) {
        if (world instanceof ServerWorld
                && isMature(state)
                && entity instanceof LivingEntity livingEntity) {
            livingEntity.addStatusEffect(ModEffects.NATURAL_SPORES);
            world.setBlockState(pos, state.with(AGE, this.getMaxAge() - 1), 3);
        }
    }

    @Override
    protected void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        WeedGrowNotifier.notifyEvent(world, pos);
        StrangleFernBlockEntity sfbe = (StrangleFernBlockEntity) world.getBlockEntity(pos);
        if (sfbe != null && !oldState.isOf(this)) {
            sfbe.setInfestedState(oldState);
        }
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
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new StrangleFernBlockEntity(pos, state);
    }

    @Override
    public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state) {
        return !isMature(state);
    }

    @Override
    public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
        return (double)random.nextFloat() < 0.4;
    }

    @Override
    public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
        ageUp(state,world,pos);
    }
}
