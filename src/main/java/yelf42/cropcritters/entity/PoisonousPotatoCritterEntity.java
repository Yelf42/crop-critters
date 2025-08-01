package yelf42.cropcritters.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.EntityEffectParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import yelf42.cropcritters.CropCritters;
import yelf42.cropcritters.blocks.ModBlocks;

import java.util.function.Predicate;

public class PoisonousPotatoCritterEntity extends AbstractCropCritterEntity implements Monster {

    private static final EntityEffectParticleEffect PARTICLE_EFFECT = EntityEffectParticleEffect.create(ParticleTypes.ENTITY_EFFECT, ColorHelper.withAlpha(1F, 8889187));

    private static final Predicate<Entity> POISON_PREDICATE = (entity) -> {
        if (entity instanceof PlayerEntity playerEntity) return !playerEntity.isCreative();
        return !entity.getType().isIn(CropCritters.CROP_CRITTERS);
    };

    protected PoisonousPotatoCritterEntity(EntityType<? extends AbstractCropCritterEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        this.targetWorkGoal = new TargetWorkGoal();
        this.goalSelector.add(8, this.targetWorkGoal);
        this.goalSelector.add(12, new WanderAroundGoal(this, 0.8));
        this.goalSelector.add(20, new LookAroundGoal(this));
    }

    @Override
    protected Predicate<BlockState> getTargetBlockFilter() {
        return (blockState -> (blockState.isOf(Blocks.FARMLAND)) || blockState.isOf(ModBlocks.SOUL_FARMLAND));
    }

    @Override
    protected int getTargetOffset() {
        return 1;
    }

    @Override
    protected boolean isHealingItem(ItemStack itemStack) {
        return false;
    }

    @Override
    protected int resetTicksUntilCanWork() {
        return MathHelper.nextInt(this.random, 600, 800);
    }

    @Override
    public void completeTargetGoal() {
        this.jump();
    }

    @Override
    public boolean canWork() {return true;}

    @Override
    public boolean isTrusting() {return false;}

    @Override
    public boolean canHaveStatusEffect(StatusEffectInstance effect) {
        return !effect.equals(StatusEffects.POISON) && super.canHaveStatusEffect(effect);
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.getWorld().isClient || this.getWorld().random.nextInt(10) != 0) return;
        double x = this.getX() + (this.random.nextDouble() - 0.5) * this.getWidth();
        double y = this.getY() + this.getHeight() * 0.5;
        double z = this.getZ() + (this.random.nextDouble() - 0.5) * this.getWidth();
        this.getWorld().addParticleClient(PARTICLE_EFFECT, x, y, z, 0, 0, 0);
    }

    @Override
    public void tickMovement() {
        super.tickMovement();
        if (this.getWorld() instanceof ServerWorld serverWorld) {
            if (this.isAlive()) {
                for(MobEntity mobEntity : serverWorld.getEntitiesByClass(MobEntity.class, this.getBoundingBox().expand(0.3), POISON_PREDICATE)) {
                    if (mobEntity.isAlive()) {
                        this.sting(serverWorld, mobEntity);
                    }
                }
            }
        }

    }

    private void sting(ServerWorld world, MobEntity target) {
        if (target.damage(world, this.getDamageSources().mobAttack(this), (float)(1))) {
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 20 * 5, 0), this);
            this.playSound(SoundEvents.ENTITY_PUFFER_FISH_STING, 1.0F, 1.0F);
        }
    }
}
