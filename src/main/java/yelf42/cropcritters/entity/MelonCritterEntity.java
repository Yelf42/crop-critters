package yelf42.cropcritters.entity;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.block.Fertilizable;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import yelf42.cropcritters.CropCritters;
import yelf42.cropcritters.items.ModItems;

import java.util.*;
import java.util.function.Predicate;

public class MelonCritterEntity extends AbstractCropCritterEntity implements RangedAttackMob {

    int wateringDuration = -1;

    public MelonCritterEntity(EntityType<? extends TameableEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected void initGoals() {
        net.minecraft.entity.ai.goal.TemptGoal temptGoal = new TemptGoal(this, 0.6, (stack) -> stack.isOf(ModItems.LOST_SOUL), false);
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(2, temptGoal);
        this.targetWorkGoal = new TargetWorkGoal();
        this.goalSelector.add(3, this.targetWorkGoal);
        this.goalSelector.add(4, new WateringGoal());
        this.targetSelector.add(7, new MelonActiveTargetGoal());
        this.goalSelector.add(7, new ProjectileAttackGoal(this, 1.25F, 20, 10.0F));
        this.goalSelector.add(12, new WanderAroundGoal(this, 0.8));
        this.goalSelector.add(20, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
        this.goalSelector.add(20, new LookAroundGoal(this));
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.MAX_HEALTH, 16)
                .add(EntityAttributes.MOVEMENT_SPEED, 0.2)
                .add(EntityAttributes.ATTACK_DAMAGE, 1)
                .add(EntityAttributes.FOLLOW_RANGE, 10)
                .add(EntityAttributes.TEMPT_RANGE, 10);
    }

    @Override
    protected Predicate<BlockState> getTargetBlockFilter() {
        return (blockState -> blockState.getBlock() instanceof CropBlock);
    }

    @Override
    protected  int getTargetOffset() {return 0;}

    @Override
    public void completeTargetGoal() {
        wateringDuration = 80;
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        if (this.isInvulnerableTo(world, source)) {
            return false;
        } else {
            this.targetWorkGoal.cancel();
            this.wateringDuration = -1;
            return super.damage(world, source, amount);
        }
    }

    @Override
    protected void handleTicksUntilCanWork() {
        if (this.ticksUntilCanWork > 0 && this.wateringDuration <= 0) {
            --this.ticksUntilCanWork;
        }
    }

    @Override
    protected Pair<Item, Integer> getLoot() {
        return new Pair<>(Items.MELON_SLICE, 8);
    }

    @Override
    protected boolean isHealingItem(ItemStack itemStack) {
        return itemStack.isOf(Items.MELON_SLICE) || itemStack.isOf(Items.MELON) || itemStack.isOf(Items.MELON_SEEDS);
    }

    @Override
    protected int resetTicksUntilCanWork() {
        return MathHelper.nextInt(this.random, 300, 500);
    }

    @Override
    protected boolean canWork() {
        return super.canWork() && this.wateringDuration <= 0;
    }

    @Override
    public void shootAt(LivingEntity target, float pullProgress) {
        double d = target.getX() - this.getX();
        double e = target.getEyeY() - 0.4F;
        double f = target.getZ() - this.getZ();
        double g = Math.sqrt(d * d + f * f) * (double)0.2F;
        World var12 = this.getWorld();
        if (var12 instanceof ServerWorld serverWorld) {
            ItemStack itemStack = new ItemStack(Items.MELON_SEEDS);
            ProjectileEntity.spawn(new SpitSeedProjectileEntity(serverWorld, this, itemStack), serverWorld, itemStack, (entity) -> entity.setVelocity(d, e + g - entity.getY(), f, 1.2F, 3.0F));
        }
        this.playSound(SoundEvents.UI_HUD_BUBBLE_POP, 9.0F, 0.4F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
    }

    class WateringGoal extends Goal {
        List<BlockPos> wateringTargets;

        WateringGoal() {
            this.setControls(EnumSet.of(Control.LOOK));
        }

        @Override
        public boolean canStart() {
            if (MelonCritterEntity.this.wateringDuration <= 0) return false;
            wateringTargets = new ArrayList<>(3);
            findWateringTargets();
            if (wateringTargets.isEmpty()) {
                MelonCritterEntity.this.wateringDuration = 0;
                return false;
            }
            return true;
        }

        @Override
        public void start() {
            MelonCritterEntity.this.navigation.stop();
        }

        @Override
        public boolean shouldContinue() {
            return MelonCritterEntity.this.wateringDuration > 0;
        }

        @Override
        public void tick() {
            MelonCritterEntity.this.wateringDuration--;
            MelonCritterEntity.this.navigation.stop();
            if (MelonCritterEntity.this.random.nextInt(10) == 0) {
                BlockPos toWater = wateringTargets.get(MelonCritterEntity.this.random.nextInt(wateringTargets.size()));
                World world = MelonCritterEntity.this.getWorld();
                BlockState toWaterState = world.getBlockState(toWater);
                if (toWaterState.getBlock() instanceof Fertilizable fertilizable) {
                    if (fertilizable.isFertilizable(world, toWater, toWaterState)) {
                        if (world instanceof ServerWorld) {
                            if (fertilizable.canGrow(world, world.random, toWater, toWaterState)) {
                                fertilizable.grow((ServerWorld) world, world.random, toWater, toWaterState);
                            }
                        }
                    }
                }
            }
            Vec3d facing = MelonCritterEntity.this.getRotationVector().normalize().multiply(3);
            Vec3d start = MelonCritterEntity.this.getPos().add(0F, 0.1F, 0F);

            CropCritters.WaterSprayS2CPayload payload = new CropCritters.WaterSprayS2CPayload(start, facing);

            for (ServerPlayerEntity player : PlayerLookup.world((ServerWorld) MelonCritterEntity.this.getWorld())) {
                if (start.isInRange(player.getPos(), 64)) {
                    ServerPlayNetworking.send(player, payload);
                }
            }
            MelonCritterEntity.this.playSound(SoundEvents.WEATHER_RAIN, 0.1F, 0.8F / (MelonCritterEntity.this.getRandom().nextFloat() * 0.4F + 0.8F));

        }

        private void findWateringTargets() {
            Vec3d facing = MelonCritterEntity.this.getFacing().getDoubleVector().normalize();
            BlockPos start = MelonCritterEntity.this.getBlockPos();
            float stepSize = 0.3F;
            for (int i = 0; i < 10; i++) {
                Vec3d offset = facing.multiply(i * stepSize);
                BlockPos check = start.add(Math.round((float)offset.x), 0 , Math.round((float)offset.z));
                if (!wateringTargets.contains(check)) wateringTargets.add(check);
            }
        }
    }

    class MelonActiveTargetGoal extends ActiveTargetGoal<LivingEntity> {

        public MelonActiveTargetGoal() {
            super(MelonCritterEntity.this, LivingEntity.class, 10, true, false, (entity, serverWorld) -> (!(entity.getType().isIn(CropCritters.CROP_CRITTERS))));
        }

        @Override
        protected void findClosestTarget() {
            ServerWorld serverWorld = getServerWorld(this.mob);
            this.targetEntity = serverWorld.getClosestEntity(this.mob.getWorld().getEntitiesByClass(this.targetClass, this.getSearchBox(this.getFollowRange()), (livingEntity) -> true), this.getAndUpdateTargetPredicate(), this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
        }

        private TargetPredicate getAndUpdateTargetPredicate() {
            return this.targetPredicate.setBaseMaxDistance(this.getFollowRange());
        }

        @Override
        public boolean canStart() {
            return super.canStart() && !MelonCritterEntity.this.isTrusting();
        }

        @Override
        public boolean shouldContinue() {
            return super.shouldContinue() && !MelonCritterEntity.this.isTrusting();
        }
    }
}
