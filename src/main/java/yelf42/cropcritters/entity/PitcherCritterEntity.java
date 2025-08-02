package yelf42.cropcritters.entity;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Pair;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.animatable.processing.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.constant.DefaultAnimations;
import yelf42.cropcritters.items.ModItems;

import java.util.function.Predicate;

public class PitcherCritterEntity extends AbstractCropCritterEntity {

    public static final RawAnimation EAT = RawAnimation.begin().thenPlay("attack.eat");

    private final TargetPredicate.EntityPredicate CAN_EAT = (entity, world) -> {
        if (this.consume > 0
                || entity instanceof PitcherCritterEntity
                || (entity.getBoundingBox().getLengthX() > 1.0)
                || (entity.getBoundingBox().getLengthY() > 1.0)
                || entity.isInvulnerable())
            return false;
        if (this.isTrusting()) {
            return !entity.hasCustomName() && (!(entity instanceof TameableEntity tameableEntity) || !tameableEntity.isTamed());
        }
        return true;
    };

    private int consume = 0;
    private boolean timeToConsume = false;
    private Entity consumptionTarget;
    private float lookAtPreyAngle = 0;

    public PitcherCritterEntity(EntityType<? extends TameableEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(DefaultAnimations.genericWalkIdleController(),
                new AnimationController<>("eat_controller", animTest -> PlayState.STOP)
                .triggerableAnim("eat", EAT));
    }

    @Override
    protected void initGoals() {
        net.minecraft.entity.ai.goal.TemptGoal temptGoal = new TemptGoal(this, 0.6, (stack) -> stack.isOf(ModItems.LOST_SOUL), false);
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(2, temptGoal);
        this.goalSelector.add(4, new ActiveTargetGoal<>(this, LivingEntity.class, 0, true, true, (entity, world) -> CAN_EAT.test(entity, world)));
        this.goalSelector.add(4, new AttackGoal(this));
        this.goalSelector.add(12, new WanderAroundGoal(this, 0.8));
        this.goalSelector.add(20, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
        this.goalSelector.add(20, new LookAroundGoal(this));
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.MAX_HEALTH, 16)
                .add(EntityAttributes.MOVEMENT_SPEED, 0.15)
                .add(EntityAttributes.ATTACK_DAMAGE, 1)
                .add(EntityAttributes.FOLLOW_RANGE, 10)
                .add(EntityAttributes.TEMPT_RANGE, 10);
    }

    @Override
    protected Predicate<BlockState> getTargetBlockFilter() {return null;}
    @Override
    protected int getTargetOffset() {return 0;}
    @Override
    public void completeTargetGoal() {}

    @Override
    protected Pair<Item, Integer> getLoot() {
        return new Pair<>(Items.PITCHER_PLANT, 1);
    }

    @Override
    protected boolean isHealingItem(ItemStack itemStack) {
        return itemStack.isOf(Items.PITCHER_PLANT) || itemStack.isOf(Items.PITCHER_POD);
    }
    @Override
    protected int resetTicksUntilCanWork() {
        return MathHelper.nextInt(this.random, 100, 200);
    }
    @Override
    protected boolean canWork() {return true;}

    @Override
    public void tick() {
        super.tick();
        if (this.getWorld().isClient) return;
        if (this.consumptionTarget == null) {
            this.consume = 0;
            this.timeToConsume = false;
        } else {
            if (this.consume > 0) {
                Vec3d mouth = this.getPos().add(0, this.getStandingEyeHeight() * 0.5, 0);
                Vec3d dir = mouth.subtract(this.consumptionTarget.getPos()).normalize().multiply(0.2);
                this.setYaw(lookAtPreyAngle);
                this.consumptionTarget.setVelocity(dir);
                this.consume--;
            }
            if (this.consume <= 1 && this.timeToConsume) {
                consume(this.getWorld(), this.consumptionTarget);
            }
        }
    }

    @Override
    public boolean tryAttack(ServerWorld world, Entity target) {
        if (this.consume > 0) return false;
        target.noClip = true;
        target.setSilent(true);
        target.setInvulnerable(true);
        target.setNoGravity(true);
        this.consume = 10;
        this.timeToConsume = true;
        this.consumptionTarget = target;
        Vec3d mouth = this.getPos().add(0, this.getStandingEyeHeight() * 0.5, 0);
        Vec3d dir = mouth.subtract(this.consumptionTarget.getPos()).normalize().multiply(0.2);
        this.lookAtPreyAngle = (float)(MathHelper.atan2(-dir.z, -dir.x) * (180F / Math.PI)) - 90F;
        triggerAnim("eat_controller", "eat");
        this.playSound(SoundEvents.ENTITY_FROG_EAT, 1F, 1F);
        return true;
    }

    private void consume(World world, Entity target) {
        if (world.isClient) return;
        target.discard();
        Vec3d pos = target.getPos();
        this.heal(1.f);
        ItemEntity item = new ItemEntity(world, pos.x, pos.y, pos.z, new ItemStack(ModItems.STRANGE_FERTILIZER));
        world.spawnEntity(item);
        this.timeToConsume = false;
        this.consumptionTarget = null;
    }

    @Override
    public void onDeath(DamageSource damageSource) {
        if (this.consumptionTarget != null) this.consumptionTarget.discard();
        super.onDeath(damageSource);
    }
}
