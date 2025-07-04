package yelf42.cropcritters.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.util.GeckoLibUtil;
import yelf42.cropcritters.CropCritters;
import yelf42.cropcritters.items.ModItems;

import java.util.function.Predicate;

public class CropCritterEntity extends TameableEntity implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private static final TrackedData<Boolean> TRUSTING = DataTracker.registerData(CropCritterEntity.class, TrackedDataHandlerRegistry.BOOLEAN);;
    public static final Predicate<Entity> NOTICEABLE_PLAYER_FILTER = (entity) -> !entity.isSneaky() && EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR.test(entity);

    public CropCritterEntity(EntityType<? extends TameableEntity> entityType, World world) {
        super(entityType, world);
        this.setPathfindingPenalty(PathNodeType.DANGER_OTHER, 0.0F);
        this.setPathfindingPenalty(PathNodeType.DAMAGE_OTHER, 0.0F);
    }

    public void setTrusting(boolean trusting) {
        this.dataTracker.set(TRUSTING, trusting);
    }
    public boolean isTrusting() {
        return (Boolean)this.dataTracker.get(TRUSTING);
    }

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        view.putBoolean("Trusting", this.isTrusting());
    }

    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view);
        this.setTrusting(view.getBoolean("Trusting", false));
    }

    @Override
    public @Nullable PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return null;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(DefaultAnimations.genericLivingController());
    }

    protected void initGoals() {
        net.minecraft.entity.ai.goal.TemptGoal temptGoal = new TemptGoal(this, 0.6, (stack) -> stack.isOf(ModItems.LOST_SOUL), true);
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(2, temptGoal);
        this.goalSelector.add(3, new FleeEntityGoal<>(this, PlayerEntity.class, 10.0F, 1.6, 1.4, (entity) -> NOTICEABLE_PLAYER_FILTER.test(entity) && !this.isTrusting()));
        this.goalSelector.add(11, new WanderAroundGoal(this, 0.8));
        this.goalSelector.add(16, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
        this.goalSelector.add(16, new LookAroundGoal(this));
    }

    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(TRUSTING, false);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.MAX_HEALTH, 18)
                .add(EntityAttributes.MOVEMENT_SPEED, 0.35)
                .add(EntityAttributes.ATTACK_DAMAGE, 1)
                .add(EntityAttributes.FOLLOW_RANGE, 20)
                .add(EntityAttributes.TEMPT_RANGE, 10);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return false;
    }

//    @Override
//    protected void mobTick(ServerWorld world) {
//        for(PrioritizedGoal prioritizedGoal : this.goalSelector.getGoals()) {
//            if (prioritizedGoal.isRunning()) {
//                CropCritters.LOGGER.info(prioritizedGoal.getGoal().getClass().toString());
//            }
//        }
//        super.mobTick(world);
//    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack itemStack = player.getStackInHand(hand);
        if (!this.getWorld().isClient()) {
            if (itemStack.isOf(ModItems.LOST_SOUL) && !this.isTrusting()) {
                this.eat(player, hand, itemStack);
                this.tryTame();
                this.setPersistent();
            } else if (itemStack.isOf(Items.WHEAT) && (this.getHealth() < this.getMaxHealth())) {
                this.eat(player, hand, itemStack);
                this.heal(1.f);
                this.getWorld().sendEntityStatus(this, (byte)7);
            }
            return ActionResult.SUCCESS;
        }

        ActionResult actionResult = super.interactMob(player, hand);
        if (actionResult.isAccepted()) {
            this.setPersistent();
        }

        return actionResult;
    }

    private void tryTame() {
        if (this.random.nextInt(3) == 0) {
            this.setTrusting(true);
            this.getWorld().sendEntityStatus(this, (byte)7);
        } else {
            this.getWorld().sendEntityStatus(this, (byte)6);
        }

    }

    static class TemptGoal extends net.minecraft.entity.ai.goal.TemptGoal {
        @Nullable
        private PlayerEntity player;
        private final CropCritterEntity critter;

        public TemptGoal(CropCritterEntity critter, double speed, Predicate<ItemStack> foodPredicate, boolean canBeScared) {
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
