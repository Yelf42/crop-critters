package yelf42.cropcritters.blocks;

import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.collection.Pool;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.tick.TickPriority;
import yelf42.cropcritters.CropCritters;
import yelf42.cropcritters.events.WeedGrowNotifier;
import yelf42.cropcritters.features.ModFeatures;
import yelf42.cropcritters.sound.ModSounds;

public class PuffbombPlantBlock extends MushroomPlantBlock {

    public static final int MAX_AGE = 2;
    public static final IntProperty AGE = Properties.AGE_2;
    private static final VoxelShape[] SHAPES_BY_AGE = Block.createShapeArray(2, age -> Block.createColumnShape(5 + age * 4, -1.0, 5 + age * 4));

    private static final RegistryKey<ConfiguredFeature<?, ?>> FEATURE_KEY = ModFeatures.PUFFBOMB_BLOB_CONFIGURED_FEATURE;

    private static final ExplosionBehavior BURST = new ExplosionBehavior() {
        public boolean canDestroyBlock(Explosion explosion, BlockView world, BlockPos pos, BlockState state, float power) {
            return false;
        }
        public boolean shouldDamage(Explosion explosion, Entity entity) {
            return false;
        }
    };

    public PuffbombPlantBlock(Settings settings) {
        super(FEATURE_KEY, settings);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPES_BY_AGE[this.getAge(state)];
    }

    @Override
    protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        BlockPos blockPos = pos.down();
        BlockState blockState = world.getBlockState(blockPos);
        if (blockState.isIn(BlockTags.MUSHROOM_GROW_BLOCK) || blockState.isIn(BlockTags.DIRT)) {
            return true;
        } else {
            return world.getBaseLightLevel(pos, 0) < 13 && this.canPlantOnTop(blockState, world, blockPos);
        }
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

    public BlockState withAge(int age) {
        return this.getDefaultState().with(this.getAgeProperty(), age);
    }

    public final boolean isMature(BlockState state) {
        return this.getAge(state) >= this.getMaxAge();
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        switch(getAge(state)) {
            case 0:
                world.scheduleBlockTick(pos, state.getBlock(), 40, TickPriority.EXTREMELY_LOW);
                break;
            case 1:
                world.playSound(null, pos, ModSounds.TICKING, SoundCategory.BLOCKS, 0.2f, 0.8f + 0.05f * (float)random.nextInt(8));
                world.scheduleBlockTick(pos, state.getBlock(), 20, TickPriority.EXTREMELY_LOW);
                break;
            case 2:
                world.playSound(null, pos, ModSounds.TICKING, SoundCategory.BLOCKS, 0.2f, 0.8f + 0.05f * (float)random.nextInt(8));
                world.scheduleBlockTick(pos, state.getBlock(), 10, TickPriority.EXTREMELY_LOW);
                break;
            default:
                CropCritters.LOGGER.info("Puffbomb age is returning a weird value");
                break;
        }
    }

    @Override
    protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (random.nextInt(2) == 0) return;
        if (isMature(state)) {
            grow(world, random, pos, state);
        } else {
            world.setBlockState(pos, this.withAge(this.getAge(state) + 1), Block.NOTIFY_LISTENERS);
        }
    }

    @Override
    public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
        if (isMature(state)) {
            world.createExplosion(null, null, BURST, pos.getX(), pos.getY(), pos.getZ(), 3F, false, World.ExplosionSourceType.BLOCK, ParticleTypes.EXPLOSION, ParticleTypes.EXPLOSION_EMITTER, Pool.empty(), ModSounds.PUFFBOMB_EXPLODE);
            super.grow(world, random, pos, state);
            return;
        }
        world.setBlockState(pos, this.withAge(this.getAge(state) + 1), Block.NOTIFY_LISTENERS);
    }

    @Override
    protected void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        WeedGrowNotifier.notifyEvent(world, pos);
        world.scheduleBlockTick(pos, state.getBlock(), 40, TickPriority.EXTREMELY_LOW);
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
}
