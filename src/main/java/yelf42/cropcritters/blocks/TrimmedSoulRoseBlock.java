package yelf42.cropcritters.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.*;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.tick.TickPriority;
import yelf42.cropcritters.CropCritters;
import yelf42.cropcritters.area_affectors.AffectorPositions;
import yelf42.cropcritters.area_affectors.AffectorType;
import yelf42.cropcritters.area_affectors.TypedBlockArea;
import yelf42.cropcritters.entity.TorchflowerCritterEntity;

import java.util.Collection;
import java.util.List;

public class TrimmedSoulRoseBlock extends PlantBlock {
    public static final MapCodec<TrimmedSoulRoseBlock> CODEC = createCodec(TrimmedSoulRoseBlock::new);
    private static final VoxelShape SHAPE = Block.createColumnShape((double)8.0F, (double)0.0F, (double)8.0F);

    public MapCodec<? extends TrimmedSoulRoseBlock> getCodec() {
        return CODEC;
    }

    public TrimmedSoulRoseBlock(AbstractBlock.Settings settings) {
        super(settings);
    }

    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    protected boolean canPlantOnTop(BlockState floor, BlockView world, BlockPos pos) {
        BlockState blockState = world.getBlockState(pos);
        return blockState.isIn(BlockTags.DIRT) || blockState.isSideSolid(world, pos, Direction.UP, SideShapeType.CENTER);
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        AffectorPositions affectorPositions = world.getAttachedOrElse(
                CropCritters.AFFECTOR_POSITIONS_ATTACHMENT_TYPE,
                AffectorPositions.EMPTY
        );
        Collection<? extends TypedBlockArea> affectorsInSection = affectorPositions.getAffectorsInSection(pos);
        if (!affectorsInSection.isEmpty()) {
            for (TypedBlockArea typedBlockArea : affectorsInSection) {
                if (typedBlockArea.blockArea().isPositionInside(pos)) {
                    world.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME,
                            pos.getX() + 0.5,
                            pos.getY() + 0.55,
                            pos.getZ() + 0.5,
                            1,
                            0.0, 0.0, 0.0,
                            0.0
                    );
                    world.scheduleBlockTick(pos, ModBlocks.TRIMMED_SOUL_ROSE, 20 + random.nextInt(60), TickPriority.EXTREMELY_LOW);
                }
            }
        } else {
            world.scheduleBlockTick(pos, ModBlocks.TRIMMED_SOUL_ROSE, 400, TickPriority.EXTREMELY_LOW);
        }
    }

    @Override
    protected void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        world.scheduleBlockTick(pos, ModBlocks.TRIMMED_SOUL_ROSE, 40, TickPriority.EXTREMELY_LOW);
        super.onBlockAdded(state, world, pos, oldState, notify);
    }

    @Override
    protected boolean hasRandomTicks(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        world.scheduleBlockTick(pos, ModBlocks.TRIMMED_SOUL_ROSE, 40, TickPriority.EXTREMELY_LOW);
    }

}