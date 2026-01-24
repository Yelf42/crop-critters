package yelf42.cropcritters.entity;

import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.object.PlayState;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.util.GeckoLibUtil;
import yelf42.cropcritters.CropCritters;
import yelf42.cropcritters.config.ConfigManager;
import yelf42.cropcritters.items.ModItems;

import java.util.EnumSet;
import java.util.Optional;
import java.util.function.Predicate;

public abstract class AbstractCropCritterEntity extends TameableEntity implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private static final TrackedData<Boolean> TRUSTING = DataTracker.registerData(AbstractCropCritterEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final Predicate<Entity> NOTICEABLE_PLAYER_FILTER = (entity) -> !entity.isSneaky() && EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR.test(entity);
    private static final Predicate<Entity> FARM_ANIMALS_FILTER = (entity -> entity.getType().isIn(CropCritters.SCARE_CRITTERS));
    public static final RawAnimation SIT = RawAnimation.begin().thenLoop("animated.sit");

    // Override these methods
    protected abstract Predicate<BlockState> getTargetBlockFilter();
    protected abstract int getTargetOffset();
    protected abstract boolean isHealingItem(ItemStack itemStack);
    protected abstract int resetTicksUntilCanWork();
    public abstract void completeTargetGoal();
    protected abstract Pair<Item, Integer> getLoot();

    protected  int resetTicksUntilCanWork(int work) {
        return (int) ((double)work * ConfigManager.CONFIG.critterWorkSpeedMultiplier);
    }

    // Override for more complex behaviours
    protected boolean canWork() {return this.isTrusting();}
    public boolean isShaking() {return false;}

    @Nullable
    BlockPos targetPos;
    TargetWorkGoal targetWorkGoal;

    int ticksUntilCanWork = 20 * 10;

    public AbstractCropCritterEntity(EntityType<? extends TameableEntity> entityType, World world) {
        super(entityType, world);
    }

    public void setTrusting(boolean trusting) {
        this.dataTracker.set(TRUSTING, trusting);
    }
    public boolean isTrusting() {
        return this.dataTracker.get(TRUSTING);
    }

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        view.putBoolean("Trusting", this.isTrusting());
        view.putInt("TicksUntilCanWork", this.ticksUntilCanWork);
    }

    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view);
        this.setTrusting(view.getBoolean("Trusting", false));
        this.ticksUntilCanWork = view.getInt("TicksUntilCanWork", resetTicksUntilCanWork());
        this.targetPos = null;
    }


    @Override
    public @Nullable PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {return null;}

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(
                new AnimationController<>("Sit", test -> {
                    if ((this.dataTracker.get(TAMEABLE_FLAGS) & 0x01) != 0) {
                        return (test.setAndContinue(SIT));
                    }
                    test.controller().reset();
                    return PlayState.STOP;
                }),
                DefaultAnimations.genericWalkIdleController()
        );
    }

    protected void initGoals() {
        net.minecraft.entity.ai.goal.TemptGoal temptGoal = new TemptGoal(this, 0.6, (stack) -> stack.isOf(ModItems.LOST_SOUL), true);
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(2, temptGoal);
        this.goalSelector.add(4, new FleeEntityGoal<>(this, AnimalEntity.class, 10.0F, 1.2, 1.4, FARM_ANIMALS_FILTER::test));
        this.goalSelector.add(6, new FleeEntityGoal<>(this, PlayerEntity.class, 10.0F, 1.2, 1.4, (entity) -> NOTICEABLE_PLAYER_FILTER.test(entity) && !this.isTrusting()));
        this.targetWorkGoal = new TargetWorkGoal();
        this.goalSelector.add(8, this.targetWorkGoal);
        this.goalSelector.add(12, new WanderAroundGoal(this, 0.8));
        this.goalSelector.add(20, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
        this.goalSelector.add(20, new LookAroundGoal(this));
    }

    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(TRUSTING, false);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.MAX_HEALTH, 8)
                .add(EntityAttributes.MOVEMENT_SPEED, 0.3)
                .add(EntityAttributes.ATTACK_DAMAGE, 1)
                .add(EntityAttributes.FOLLOW_RANGE, 10)
                .add(EntityAttributes.TEMPT_RANGE, 10);
    }

    @Override
    public void playAmbientSound() {
        if (this.getBoundingBox().getLengthX() > 0.51) {
            // Big
            playSound(SoundEvents.ENTITY_ALLAY_ITEM_GIVEN, 1F, 0.6F);
        } else {
            // Smol
            playSound(SoundEvents.ENTITY_ALLAY_ITEM_GIVEN);
        }
    }

    @Override
    protected void playHurtSound(DamageSource damageSource) {
        if (this.getBoundingBox().getLengthX() > 0.51) {
            // Big
            playSound(SoundEvents.ENTITY_GLOW_SQUID_DEATH, 1F, 1.1F);
            playSound(SoundEvents.ENTITY_ALLAY_HURT, 1F, 0.6F);
        } else {
            // Smol
            playSound(SoundEvents.ENTITY_ALLAY_HURT);
        }
    }

    @Override
    protected @Nullable SoundEvent getDeathSound() {
        if (this.getBoundingBox().getLengthX() > 0.51) {
            // Big
            playSound(SoundEvents.ENTITY_GLOW_SQUID_DEATH, 1F, 1.1F);
            return SoundEvents.ENTITY_ALLAY_DEATH;
        } else {
            // Smol
            playSound(SoundEvents.ENTITY_AXOLOTL_DEATH);
            return SoundEvents.ENTITY_ALLAY_DEATH;
        }
    }

    @Override
    protected EntityNavigation createNavigation(World world) {
        MobNavigation mobNavigation = new MobNavigation(this, world);
        mobNavigation.setCanSwim(true);
        return mobNavigation;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return false;
    }

/*
    @Override
    protected void mobTick(ServerWorld world) {
        for(PrioritizedGoal prioritizedGoal : this.goalSelector.getGoals()) {
            if (prioritizedGoal.isRunning()) {
                CropCritters.LOGGER.info(prioritizedGoal.getGoal().getClass().toString());
            }
        }
        super.mobTick(world);
    }
*/

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        if (this.isInvulnerableTo(world, source) || source.isOf(DamageTypes.CACTUS)) {
            return false;
        } else {
            if (this.targetWorkGoal != null) this.targetWorkGoal.cancel();
            if (source.getAttacker() instanceof PlayerEntity && source.getWeaponStack() != null && source.getWeaponStack().isIn(ItemTags.HOES)) amount *= 3;
            return super.damage(world, source, amount);
        }
    }

    @Override
    protected void drop(ServerWorld world, DamageSource damageSource) {
        Pair<Item, Integer> loot = getLoot();
        int quantity = 1;
        if (loot.getRight() > 1 && damageSource.getAttacker() instanceof PlayerEntity) {
            boolean withHoe = damageSource.getWeaponStack() != null && damageSource.getWeaponStack().isIn(ItemTags.HOES);
            if (withHoe) quantity = world.random.nextInt(loot.getRight()) + 1;
        }
        ItemStack toDrop = new ItemStack(loot.getLeft(), quantity);
        this.dropStack(world, toDrop);
        this.dropExperience(world, damageSource.getAttacker());
    }

    @Override
    protected int getExperienceToDrop(ServerWorld world) {
        return super.getExperienceToDrop(world) + (this.isTrusting() ? 3 : 0);
    }

    @Override
    public void tickMovement() {
        super.tickMovement();
        handleTicksUntilCanWork();
    }

    protected void handleTicksUntilCanWork() {
        if (this.ticksUntilCanWork > 0) {
            --this.ticksUntilCanWork;
        }
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack itemStack = player.getStackInHand(hand);
        if (!this.getEntityWorld().isClient()) {
            if (itemStack.isOf(ModItems.LOST_SOUL) && !this.isTrusting()) {
                this.eat(player, hand, itemStack);
                this.tryTame(player);
                this.setPersistent();
                return ActionResult.SUCCESS;
            } else if (this.isHealingItem(itemStack) && (this.getHealth() < this.getMaxHealth())) {
                this.eat(player, hand, itemStack);
                this.heal(4.f);
                this.getEntityWorld().sendEntityStatus(this, (byte)7);
                this.setPersistent();
                return ActionResult.SUCCESS;
            }
        }

        return ActionResult.PASS;
    }

    protected void tryTame(PlayerEntity player) {
        if (this.random.nextInt(2) == 0) {
            this.setTrusting(true);
            this.setTamedBy(player);
            this.getEntityWorld().sendEntityStatus(this, (byte)7);
            float newHealth = this.getMaxHealth() * 2F;
            this.getAttributeInstance(EntityAttributes.MAX_HEALTH).setBaseValue(newHealth);
            this.setHealth(newHealth);
        } else {
            this.getEntityWorld().sendEntityStatus(this, (byte)6);
        }
    }

    void clearTargetPos() {
        this.targetPos = null;
        this.ticksUntilCanWork = resetTicksUntilCanWork();
    }

    public boolean isAttractive(BlockPos pos) {
        BlockState target = this.getEntityWorld().getBlockState(pos);
        BlockState above = this.getEntityWorld().getBlockState(pos.up());
        return this.getTargetBlockFilter().test(target) && above.isOf(Blocks.AIR);
    }

    class TargetWorkGoal extends Goal {
        protected Long2LongOpenHashMap unreachableTargetsPosCache = new Long2LongOpenHashMap();
        protected boolean running;
        protected int ticks;
        protected Vec3d nextTarget;

        TargetWorkGoal() {
            this.setControls(EnumSet.of(Control.MOVE));
        }

        public void start() {
            this.running = true;
            this.ticks = 0;
        }

        public void stop() {
            this.running = false;
            AbstractCropCritterEntity.this.navigation.stop();
            AbstractCropCritterEntity.this.clearTargetPos();
        }

        @Override
        public boolean canStart() {
            if (AbstractCropCritterEntity.this.ticksUntilCanWork > 0) return false;
            if (!AbstractCropCritterEntity.this.canWork()) return false;
            Optional<BlockPos> optional = this.getTargetBlock();
            if (optional.isPresent()) {
                AbstractCropCritterEntity.this.targetPos = optional.get();
                return true;
            } else {
                AbstractCropCritterEntity.this.ticksUntilCanWork = 80;
                return false;
            }
        }

        @Override
        public boolean shouldContinue() {
            return this.running && (AbstractCropCritterEntity.this.targetPos != null);
        }

        public void tick() {
            if (AbstractCropCritterEntity.this.targetPos != null) {
                ++this.ticks;
                if (this.ticks > 600 || !(isAttractive(AbstractCropCritterEntity.this.targetPos))) {
                    AbstractCropCritterEntity.this.clearTargetPos();
                } else {
                    Vec3d vec3d = Vec3d.ofBottomCenter(AbstractCropCritterEntity.this.targetPos).add(0.0F, getTargetOffset(), 0.0F);
                    if (vec3d.squaredDistanceTo(AbstractCropCritterEntity.this.getEntityPos()) > (double)1.0F) {
                        this.nextTarget = vec3d;
                        this.moveToNextTarget();
                    } else {
                        if (this.nextTarget == null) {
                            this.nextTarget = vec3d;
                        }

                        boolean bl = AbstractCropCritterEntity.this.getEntityPos().distanceTo(this.nextTarget) <= 0.5;
                        if (!bl && this.ticks > 600) {
                            AbstractCropCritterEntity.this.clearTargetPos();
                        } else if (bl) {
                            // At target pos
                            AbstractCropCritterEntity.this.completeTargetGoal();
                            AbstractCropCritterEntity.this.clearTargetPos();
                        } else {
                            AbstractCropCritterEntity.this.getMoveControl().moveTo(this.nextTarget.getX(), this.nextTarget.getY(), this.nextTarget.getZ(), 1.2F);
                        }
                    }
                }
            }
        }

        protected void moveToNextTarget() {
            AbstractCropCritterEntity.this.navigation.startMovingAlong(AbstractCropCritterEntity.this.navigation.findPathTo(this.nextTarget.getX(), this.nextTarget.getY(), this.nextTarget.getZ(), 0), 1F);
        }

        void cancel() {
            this.running = false;
        }

        protected Optional<BlockPos> getTargetBlock() {
            Iterable<BlockPos> iterable = BlockPos.iterateOutwards(AbstractCropCritterEntity.this.getBlockPos(), 6, 3, 6);
            Long2LongOpenHashMap long2LongOpenHashMap = new Long2LongOpenHashMap();

            for(BlockPos blockPos : iterable) {
                long l = this.unreachableTargetsPosCache.getOrDefault(blockPos.asLong(), Long.MIN_VALUE);
                if (AbstractCropCritterEntity.this.getEntityWorld().getTime() < l) {
                    long2LongOpenHashMap.put(blockPos.asLong(), l);
                } else if (isAttractive(blockPos)) {
                    Path path = AbstractCropCritterEntity.this.navigation.findPathTo(blockPos, 0);
                    if (path != null && path.reachesTarget()) {
                        return Optional.of(blockPos);
                    }

                    long2LongOpenHashMap.put(blockPos.asLong(), AbstractCropCritterEntity.this.getEntityWorld().getTime() + 600L);
                }
            }

            this.unreachableTargetsPosCache = long2LongOpenHashMap;
            return Optional.empty();
        }
    }



    static class TemptGoal extends net.minecraft.entity.ai.goal.TemptGoal {
        @Nullable
        private PlayerEntity player;
        private final AbstractCropCritterEntity critter;

        public TemptGoal(AbstractCropCritterEntity critter, double speed, Predicate<ItemStack> foodPredicate, boolean canBeScared) {
            super(critter, speed, foodPredicate, canBeScared);
            this.critter = critter;
        }

        public void tick() {
            super.tick();
            if (this.player == null && this.mob.getRandom().nextInt(this.getTickCount(600)) == 0) {
                this.player = this.closestPlayer;
            } else if (this.mob.getRandom().nextInt(this.getTickCount(500)) == 0) {
                this.player = null;
            }

        }

        protected boolean canBeScared() {
            return (this.player == null || !this.player.equals(this.closestPlayer)) && super.canBeScared();
        }

        public boolean canStart() {
            return super.canStart() && !this.critter.isTrusting();
        }
    }
}
