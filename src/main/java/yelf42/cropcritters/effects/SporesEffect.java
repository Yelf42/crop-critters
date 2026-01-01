package yelf42.cropcritters.effects;

import net.minecraft.block.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.particle.TintedParticleEffect;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColorHelper;
import yelf42.cropcritters.blocks.ModBlocks;
import yelf42.cropcritters.blocks.StrangleFern;
import yelf42.cropcritters.particle.ModParticles;

// TODO custom particle effect

public class SporesEffect extends StatusEffect {
    protected SporesEffect(StatusEffectCategory category, int color) {
        super(category, color, TintedParticleEffect.create(ModParticles.SPORE_PARTICLE, ColorHelper.withAlpha(100, color)));
    }

    @Override
    public boolean applyUpdateEffect(ServerWorld world, LivingEntity entity, int amplifier) {
        BlockState toPlant = ModBlocks.STRANGLE_FERN.getDefaultState();
        if (entity instanceof LivingEntity) {
            BlockPos pos = BlockPos.ofFloored(entity.getEntityPos().add(0.0, 0.5, 0.0));

            boolean canPlant = StrangleFern.canInfest(world.getBlockState(pos));
            if (canPlant) {
                world.setBlockState(pos, toPlant);
                reduceDuration(entity);
            }
            if (!world.getFluidState(pos).isEmpty()) {
                entity.removeStatusEffect(ModEffects.SPORES);
            }
        }

        return super.applyUpdateEffect(world, entity, amplifier);
    }

    private void reduceDuration(LivingEntity entity) {
        int duration = entity.getStatusEffect(ModEffects.SPORES).getDuration();
        entity.removeStatusEffect(ModEffects.SPORES);
        if (duration > 1300) {
            entity.addStatusEffect(new StatusEffectInstance(ModEffects.SPORES, duration - 1200, 1, true, true, false));
        }
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return duration % 10 == 0;
    }
}
