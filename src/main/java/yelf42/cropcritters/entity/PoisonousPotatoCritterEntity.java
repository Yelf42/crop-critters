package yelf42.cropcritters.entity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.particle.TintedParticleEffect;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import yelf42.cropcritters.CropCritters;
import yelf42.cropcritters.blocks.ModBlocks;

import java.util.function.Predicate;

import static net.minecraft.block.Block.pushEntitiesUpBeforeBlockChange;

public class PoisonousPotatoCritterEntity extends AbstractCropCritterEntity implements Monster {

    private static final TintedParticleEffect PARTICLE_EFFECT = TintedParticleEffect.create(ParticleTypes.ENTITY_EFFECT, ColorHelper.withAlpha(1F, 8889187));

    private static final Predicate<Entity> POISON_PREDICATE = (entity) -> {
        if (entity instanceof PlayerEntity playerEntity) return !playerEntity.isCreative();
        return !entity.getType().isIn(CropCritters.CROP_CRITTERS);
    };

    private boolean lastTargetMature = false;
    private int destroyFarmland = 0;
    private BlockPos destroyFarmlandPos = null;

    protected PoisonousPotatoCritterEntity(EntityType<? extends AbstractCropCritterEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(4, new DestroyEggGoal(this, (double)1.0F, 3));
        this.targetWorkGoal = new TargetWorkGoal();
        this.goalSelector.add(8, this.targetWorkGoal);
        this.goalSelector.add(12, new WanderAroundGoal(this, 0.8));
        this.goalSelector.add(20, new LookAroundGoal(this));
    }

    @Override
    public void playAmbientSound() {
        playSound(SoundEvents.ENTITY_VEX_AMBIENT);
    }

    @Override
    protected Predicate<BlockState> getTargetBlockFilter() {
        return (blockState -> (blockState.getBlock() instanceof CropBlock));
    }

    @Override
    protected int getTargetOffset() {
        return 0;
    }

    @Override
    protected Pair<Item, Integer> getLoot() {
        return new Pair<>(Items.POISONOUS_POTATO, 2);
    }

    @Override
    protected boolean isHealingItem(ItemStack itemStack) {
        return false;
    }

    @Override
    protected int resetTicksUntilCanWork() {
        return resetTicksUntilCanWork(this.lastTargetMature ? MathHelper.nextInt(this.random, 900, 1200) : MathHelper.nextInt(this.random, 400, 600));
    }

    @Override
    public void completeTargetGoal() {
        if (this.getEntityWorld().isClient() || this.targetPos == null) return;
        BlockState target = this.getEntityWorld().getBlockState(this.targetPos);
        this.jump();
        this.destroyFarmland = 17;
        this.destroyFarmlandPos = this.targetPos.down();
        this.lastTargetMature = (target.getBlock() instanceof CropBlock cropBlock && cropBlock.isMature(target));
    }

    @Override
    public boolean canWork() {return true;}

    @Override
    public boolean isTrusting() {return false;}

    @Override
    protected void tryTame(PlayerEntity player) {
        this.getEntityWorld().sendEntityStatus(this, (byte)6);
    }

    @Override
    public boolean canHaveStatusEffect(StatusEffectInstance effect) {
        return !effect.equals(StatusEffects.POISON) && super.canHaveStatusEffect(effect);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.getEntityWorld().isClient()) {
            if (this.destroyFarmlandPos == null) this.destroyFarmland = 0;
            if (this.destroyFarmland > 0) {
                if (this.destroyFarmland == 1 && this.getBlockPos().isWithinDistance(this.destroyFarmlandPos, 2)) {
                    BlockState soil = this.getEntityWorld().getBlockState(this.destroyFarmlandPos);
                    Block toDirt = soil.isOf(Blocks.FARMLAND) ? Blocks.DIRT : soil.isOf(ModBlocks.SOUL_FARMLAND) ? Blocks.SOUL_SAND : null;
                    if (toDirt != null) {
                        this.getEntityWorld().setBlockState(this.destroyFarmlandPos, toDirt.getDefaultState(), Block.NOTIFY_LISTENERS);
                        pushEntitiesUpBeforeBlockChange(Blocks.FARMLAND.getDefaultState(), toDirt.getDefaultState(), this.getEntityWorld(), this.destroyFarmlandPos);
                    }
                    BlockState crop = this.getEntityWorld().getBlockState(this.destroyFarmlandPos.up());
                    if (this.lastTargetMature) Block.dropStacks(crop, this.getEntityWorld(), this.destroyFarmlandPos.up());
                    this.getEntityWorld().setBlockState(this.destroyFarmlandPos.up(), Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
                    this.destroyFarmlandPos = null;
                }
                this.destroyFarmland--;
            }
        } else {
            if (this.getEntityWorld().random.nextInt(10) != 0) return;
            double x = this.getX() + (this.random.nextDouble() - 0.5) * this.getWidth();
            double y = this.getY() + this.getHeight() * 0.5;
            double z = this.getZ() + (this.random.nextDouble() - 0.5) * this.getWidth();
            this.getEntityWorld().addParticleClient(PARTICLE_EFFECT, x, y, z, 0, 0, 0);
        }
    }

    @Override
    public void tickMovement() {
        super.tickMovement();
        if (this.getEntityWorld() instanceof ServerWorld serverWorld) {
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

    class DestroyEggGoal extends StepAndDestroyBlockGoal {
        DestroyEggGoal(final PathAwareEntity mob, final double speed, final int maxYDifference) {
            super(Blocks.TURTLE_EGG, mob, speed, maxYDifference);
        }

        public void tickStepping(WorldAccess world, BlockPos pos) {
            world.playSound((Entity)null, pos, SoundEvents.ENTITY_ZOMBIE_DESTROY_EGG, SoundCategory.HOSTILE, 0.5F, 0.9F + PoisonousPotatoCritterEntity.this.random.nextFloat() * 0.2F);
        }

        public void onDestroyBlock(World world, BlockPos pos) {
            world.playSound((Entity)null, pos, SoundEvents.ENTITY_TURTLE_EGG_BREAK, SoundCategory.BLOCKS, 0.7F, 0.9F + world.random.nextFloat() * 0.2F);
        }

        public double getDesiredDistanceToTarget() {
            return 1.14;
        }
    }
}
