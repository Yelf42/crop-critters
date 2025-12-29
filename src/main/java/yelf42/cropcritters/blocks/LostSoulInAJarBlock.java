package yelf42.cropcritters.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.LanternBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.tick.TickPriority;
import org.jetbrains.annotations.Nullable;
import yelf42.cropcritters.events.WeedGrowNotifier;

public class LostSoulInAJarBlock extends LanternBlock {
    public static final BooleanProperty POWERED = Properties.POWERED;

    public LostSoulInAJarBlock(Settings settings) {
        super(settings);
        this.setDefaultState((BlockState)((BlockState)this.getDefaultState().with(POWERED, false)));
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        world.scheduleBlockTick(pos, state.getBlock(), 15, TickPriority.EXTREMELY_LOW);
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if ((Boolean)state.get(POWERED)) world.setBlockState(pos, (BlockState)state.with(POWERED, false), 3);
        if (WeedGrowNotifier.checkWeedsToRing(world, pos)) {
            world.setBlockState(pos, (BlockState)state.with(POWERED, true), 3);
            ring(world, pos, random);
        }
        world.scheduleBlockTick(pos, state.getBlock(), 15 + world.random.nextInt(30), TickPriority.EXTREMELY_LOW);
    }

    // TODO make work underground
    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (random.nextDouble() <= 1.0) {
            double d = (double)pos.getX() + random.nextDouble() * (double)10.0F - (double)5.0F;
            double e = (double)pos.getY() - 3;
            double f = (double)pos.getZ() + random.nextDouble() * (double)10.0F - (double)5.0F;
            world.addParticleClient(ParticleTypes.GLOW, d, world.getTopY(Heightmap.Type.WORLD_SURFACE, (int)d, (int)f), f, 0.0F, 1.0F, 0.0F);
        }
    }

    public void ring(World world, BlockPos pos, Random random) {
        world.playSound(null, pos, SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value(), SoundCategory.BLOCKS, 2.0f, 1.0f + 0.5f * (float)random.nextInt(7));

        world.addSyncedBlockEvent(pos, this, 0, random.nextInt(360));
    }

    @Override
    protected boolean onSyncedBlockEvent(BlockState state, World world, BlockPos pos, int type, int data) {
        Vec3d cPos = pos.toCenterPos();
        double angle = Math.toRadians((double)data);
        double planeOffset = 0.8f;
        double heightOffset = ((double)data / 360F) - 1.f;
        world.addParticleClient(ParticleTypes.NOTE, cPos.getX() + Math.cos(angle) * planeOffset, cPos.getY() + heightOffset, cPos.getZ() + Math.sin(angle) * planeOffset, 0.73f, 0.0F, 0.0F);

        return true;
    }

    @Override
    protected boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    protected int getComparatorOutput(BlockState state, World world, BlockPos pos, Direction direction) {
        return (Boolean)world.getBlockState(pos).get(POWERED) ? 15 : 0;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
        super.appendProperties(builder);
    }

}
