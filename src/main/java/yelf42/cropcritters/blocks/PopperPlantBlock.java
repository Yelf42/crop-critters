package yelf42.cropcritters.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.*;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import yelf42.cropcritters.entity.PopperPodEntity;
import yelf42.cropcritters.events.WeedGrowNotifier;
import yelf42.cropcritters.items.ModItems;

public class PopperPlantBlock extends PlantBlock implements Fertilizable {

    public static final MapCodec<PopperPlantBlock> CODEC = createCodec(PopperPlantBlock::new);
    public static final int MAX_AGE = 3;
    public static final IntProperty AGE = Properties.AGE_3;
    private static final VoxelShape SHAPE = Block.createColumnShape((double)16.0F, (double)0.0F, (double)13.0F);

    protected PopperPlantBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(AGE, 0));
    }

    @Override
    protected MapCodec<? extends PlantBlock> getCodec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    public int getMaxAge() {
        return MAX_AGE;
    }

    public int getAge(BlockState state) {
        return (Integer)state.get(AGE);
    }

    public final boolean isMature(BlockState state) {
        return this.getAge(state) >= this.getMaxAge();
    }

    @Override
    protected boolean hasRandomTicks(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        int lightLevel = world.getLightLevel(LightType.SKY, pos);
        float temp = world.getBiome(pos).value().getTemperature();
        long time = world.getTimeOfDay() % 24000;
        if (lightLevel < 14 || (time < 2000 || time > 9000) || (temp >= 1.0 || temp < 0.5)) return;

        if (!isMature(state)) {
            world.setBlockState(pos, state.with(AGE, this.getAge(state) + 1));
        } else {
            if (random.nextInt(10) == 0) popOff(state, world, pos);
        }
    }

    private void popOff(BlockState state, ServerWorld world, BlockPos pos) {
        world.setBlockState(pos, state.with(AGE, 0));

        Iterable<BlockPos> iterable = BlockPos.iterateOutwards(pos, 12,1,12);
        for(BlockPos blockPos : iterable) {
            BlockState toCheck = world.getBlockState(blockPos);
            if (toCheck.isOf(ModBlocks.POPPER_PLANT) && isMature(toCheck)) {
                world.setBlockState(blockPos, toCheck.with(AGE, 0));
                spawnPopperPod(world, blockPos);
            }
        }

        spawnPopperPod(world, pos);
    }

    private void spawnPopperPod(ServerWorld world, BlockPos pos) {
        ItemStack itemStack = new ItemStack(ModItems.POPPER_POD);
        Vec3d center = pos.toCenterPos();
        ProjectileEntity.spawn(new PopperPodEntity(world, center.x, center.y, center.z, itemStack), world, itemStack);
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
        return true;
    }

    @Override
    public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
        return random.nextInt(2) == 0;
    }

    @Override
    public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
        if (!isMature(state)) {
            world.setBlockState(pos, state.with(AGE, this.getAge(state) + 1));
        } else {
            popOff(state, world, pos);
        }
    }
}
