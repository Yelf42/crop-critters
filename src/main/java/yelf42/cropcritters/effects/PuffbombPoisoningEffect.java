package yelf42.cropcritters.effects;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.collection.Pool;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;

public class PuffbombPoisoningEffect extends StatusEffect {

    private static final ExplosionBehavior POP = new ExplosionBehavior() {
        public boolean canDestroyBlock(Explosion explosion, BlockView world, BlockPos pos, BlockState state, float power) {
            return false;
        }
        public boolean shouldDamage(Explosion explosion, Entity entity) {
            return true;
        }
    };

    protected PuffbombPoisoningEffect(StatusEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean applyUpdateEffect(ServerWorld world, LivingEntity entity, int amplifier) {
        int duration = entity.getStatusEffect(ModEffects.PUFFBOMB_POISONING).getDuration();
        if (duration <= 20) {
            BlockPos pos = BlockPos.ofFloored(entity.getEntityPos());
            world.createExplosion(null, null, POP, pos.getX(), pos.getY(), pos.getZ(), 4F, false, World.ExplosionSourceType.BLOCK, ParticleTypes.EXPLOSION, ParticleTypes.EXPLOSION_EMITTER, Pool.empty(), SoundEvents.ENTITY_BREEZE_WIND_BURST);
            entity.removeStatusEffect(ModEffects.PUFFBOMB_POISONING);
        } else {
            world.playSound(null, entity.getBlockPos(), SoundEvents.BLOCK_COPPER_BULB_TURN_ON, SoundCategory.HOSTILE, 0.5f, 0.8f + 0.05f * (float)world.random.nextInt(8));
        }

        return true;
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return duration % 20 == 0;
    }


}
