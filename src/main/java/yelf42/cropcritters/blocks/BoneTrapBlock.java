package yelf42.cropcritters.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.PlantBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCollisionHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.tick.TickPriority;
import yelf42.cropcritters.CropCritters;
import yelf42.cropcritters.events.WeedGrowNotifier;

public class BoneTrapBlock extends PlantBlock {
    public static final MapCodec<BoneTrapBlock> CODEC = createCodec(BoneTrapBlock::new);

    // 0 = open, 2 = closed, 1 = recharging
    public static final IntProperty STAGE = IntProperty.of("stage", 0, 2);

    private static final VoxelShape[] SHAPES_BY_STAGE = new VoxelShape[] {
            Block.createColumnShape(14,-1,2),
            Block.createColumnShape(13,-1,4),
            Block.createColumnShape(9,-1,9)
    };

    protected BoneTrapBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(STAGE, 0));
    }

    @Override
    protected MapCodec<? extends PlantBlock> getCodec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPES_BY_STAGE[this.getStage(state)];
    }

    public int getStage(BlockState state) {
        return (int)state.get(STAGE);
    }

    @Override
    protected boolean hasRandomTicks(BlockState state) {
        return false;
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (this.getStage(state) == 2) {
            world.setBlockState(pos, state.with(STAGE, 1));
            world.playSound(null, pos, SoundEvents.ENTITY_SKELETON_STEP, SoundCategory.BLOCKS, 0.5F, 1.0F + (world.random.nextFloat() * 0.6F - 0.3F));
            world.scheduleBlockTick(pos, this, 100 + world.random.nextInt(60), TickPriority.EXTREMELY_LOW);
        } else {
            world.playSound(null, pos, SoundEvents.ENTITY_SKELETON_STEP, SoundCategory.BLOCKS, 0.5F, 1.0F + (world.random.nextFloat() * 0.6F - 0.3F));
            world.setBlockState(pos, state.with(STAGE, 0));
        }
    }

    @Override
    protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity, EntityCollisionHandler handler, boolean bl) {
        if (entity instanceof LivingEntity livingEntity && !(livingEntity.getType().isIn(CropCritters.WEED_IMMUNE))) {
            double dist = livingEntity.getEntityPos().distanceTo(pos.toBottomCenterPos());
            if (this.getStage(state) == 0 && dist <= 0.2F) {

                world.playSound(null, pos, SoundEvents.ENTITY_EVOKER_FANGS_ATTACK, SoundCategory.BLOCKS, 0.5F, 1.0F + (world.random.nextFloat() * 0.6F - 0.3F));

                if (world instanceof ServerWorld serverWorld) {
                    livingEntity.damage(serverWorld, world.getDamageSources().sweetBerryBush(), 4.0F);
                    serverWorld.scheduleBlockTick(pos, this, 40 + serverWorld.random.nextInt(20), TickPriority.EXTREMELY_LOW);
                    serverWorld.setBlockState(pos, state.with(STAGE, 2));
                }
            } else if (this.getStage(state) == 2) {
                Vec3d vec3d = new Vec3d(0.01, 0.01, 0.01);
                livingEntity.slowMovement(state, vec3d);
            }
        }
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
        builder.add(STAGE);
    }
}
