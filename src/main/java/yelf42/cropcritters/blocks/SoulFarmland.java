package yelf42.cropcritters.blocks;

import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class SoulFarmland extends FarmlandBlock {

    public SoulFarmland(AbstractBlock.Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(MOISTURE, 7));
    }

    public static void setToSoulSoil(@Nullable Entity entity, BlockState state, World world, BlockPos pos) {
        BlockState blockState = pushEntitiesUpBeforeBlockChange(state, Blocks.SOUL_SOIL.getDefaultState(), world, pos);
        world.setBlockState(pos, blockState);
        world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(entity, blockState));
    }

    @Override
    public void onLandedUpon(World world, BlockState state, BlockPos pos, Entity entity, double fallDistance) {
        if (world instanceof ServerWorld serverWorld) {
            if ((double)world.random.nextFloat() < fallDistance - (double)0.5F && entity instanceof LivingEntity && (entity instanceof PlayerEntity || serverWorld.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)) && entity.getWidth() * entity.getWidth() * entity.getHeight() > 0.512F) {
                setToSoulSoil(entity, state, world, pos);
            }
        }
        Blocks.SOUL_SOIL.onLandedUpon(world, state, pos, entity, fallDistance);
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (!state.canPlaceAt(world, pos)) {
            setToSoulSoil((Entity)null, state, world, pos);
        }
    }

    @Override
    protected boolean hasRandomTicks(BlockState state) {
        return false;
    }
}
