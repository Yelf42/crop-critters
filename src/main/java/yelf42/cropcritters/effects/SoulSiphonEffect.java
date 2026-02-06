package yelf42.cropcritters.effects;

import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import yelf42.cropcritters.CropCritters;
import yelf42.cropcritters.area_affectors.AffectorPositions;
import yelf42.cropcritters.area_affectors.AffectorType;
import yelf42.cropcritters.area_affectors.TypedBlockArea;
import yelf42.cropcritters.config.CritterHelper;
import yelf42.cropcritters.particle.ModParticles;

import java.util.Collection;

public class SoulSiphonEffect extends StatusEffect {

    protected SoulSiphonEffect(StatusEffectCategory statusEffectCategory, int i) {
        super(statusEffectCategory, i, ModParticles.SOUL_SIPHON_PARTICLE);
    }

    public void onEntityRemoval(ServerWorld world, LivingEntity entity, int amplifier, Entity.RemovalReason reason) {
        if (reason == Entity.RemovalReason.KILLED && entity.getType().isIn(EntityTypeTags.UNDEAD)) {
            Vec3d entityPos = entity.getEntityPos();
            BlockPos pos = new BlockPos((int)entityPos.x, (int) Math.floor(entityPos.y + 0.5F), (int)entityPos.z);
            AffectorPositions affectorPositions = world.getAttachedOrElse(
                    CropCritters.AFFECTOR_POSITIONS_ATTACHMENT_TYPE,
                    AffectorPositions.EMPTY
            );
            Collection<? extends TypedBlockArea> affectorsInSection = affectorPositions.getAffectorsInSection(pos);
            if (!affectorsInSection.isEmpty()) {
                for (TypedBlockArea typedBlockArea : affectorsInSection) {
                    AffectorType type = typedBlockArea.type();
                    if (type == AffectorType.SOUL_ROSE_GOLD_3 || type == AffectorType.SOUL_ROSE_GOLD_2 || type == AffectorType.SOUL_ROSE_GOLD_1) {
                        if (typedBlockArea.blockArea().isPositionInside(pos)) {
                            growCropsAndCritters(world, pos, amplifier);
                            return;
                        }
                    }
                }
            }
        }
    }

    private void growCropsAndCritters(ServerWorld world, BlockPos pos, int amplifier) {
        Iterable<BlockPos> iterable = BlockPos.iterateOutwards(pos, amplifier, 1, amplifier);
        for(BlockPos blockPos : iterable) {
            if (world.random.nextInt(2) == 0) continue;

            BlockState blockState = world.getBlockState(blockPos);
            if (!(blockState.getBlock() instanceof PlantBlock)) continue;

            if (blockState.getBlock() instanceof Fertilizable fertilizable) {
                if (fertilizable.isFertilizable(world, blockPos, blockState)) {
                    if (world instanceof ServerWorld) {
                        if (fertilizable.canGrow(world, world.random, blockPos, blockState)) {
                            fertilizable.grow(world, world.random, blockPos, blockState);
                        }
                    }
                } else {
                    if ((blockState.getBlock() instanceof CropBlock) || (blockState.getBlock() instanceof PitcherCropBlock)) {
                        CritterHelper.spawnCritter(world, blockState, world.random, blockPos);
                    }
                }
            } else {
                if (blockState.isOf(Blocks.NETHER_WART) && blockState.get(NetherWartBlock.AGE, 0) == NetherWartBlock.MAX_AGE) {
                    CritterHelper.spawnCritter(world, blockState, world.random, blockPos);
                }
            }
        }
    }

    public boolean applyUpdateEffect(ServerWorld world, LivingEntity entity, int amplifier) {
        entity.damage(world, entity.getDamageSources().magic(), 3.0F);
        return true;
    }

    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        int i = 42 / amplifier;
        if (i > 0) {
            return duration % i == 0;
        } else {
            return true;
        }
    }
}
