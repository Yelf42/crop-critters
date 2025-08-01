package yelf42.cropcritters.entity;

import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.block.Fertilizable;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.ai.pathing.Path;
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
import yelf42.cropcritters.blocks.SoulFarmland;
import yelf42.cropcritters.items.ModItems;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class PumpkinCritterEntity extends AbstractCropCritterEntity implements RangedAttackMob {

    public PumpkinCritterEntity(EntityType<? extends TameableEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected void initGoals() {
        net.minecraft.entity.ai.goal.TemptGoal temptGoal = new TemptGoal(this, 0.6, (stack) -> stack.isOf(ModItems.LOST_SOUL), false);
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(2, temptGoal);
        this.targetWorkGoal = new PumpkinTargetWorkGoal();
        this.goalSelector.add(3, this.targetWorkGoal);
        this.targetSelector.add(7, new MelonActiveTargetGoal());
        this.goalSelector.add(7, new ProjectileAttackGoal(this, (double)1.25F, 20, 10.0F));
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
    protected Predicate<BlockState> getTargetBlockFilter() {
        return (blockState -> blockState.getBlock() instanceof FarmlandBlock || blockState.getBlock() instanceof SoulFarmland);
    }

    @Override
    protected  int getTargetOffset() {return 1;}

    @Override
    public void completeTargetGoal() {
        // TODO test when implemented
        if (this.targetPos == null) return;
        double d = this.targetPos.getX() - this.getX();
        double e = this.targetPos.getY() - 0.4F;
        double f = this.targetPos.getZ() - this.getZ();
        double g = Math.sqrt(d * d + f * f) * (double)0.2F;
        World var12 = this.getWorld();
        if (var12 instanceof ServerWorld serverWorld) {
            ItemStack itemStack = new ItemStack(ModItems.SEED_BALL);
            ProjectileEntity.spawn(new SeedBallProjectileEntity(serverWorld, this, itemStack), serverWorld, itemStack, (entity) -> entity.setVelocity(d, e + g - entity.getY(), f, 1.2F, 3.0F));
        }

        this.playSound(SoundEvents.ENTITY_LLAMA_SPIT, 1.0F, 0.4F / (this.getRandom().nextFloat() * 0.4F + 0.8F));

    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        if (this.isInvulnerableTo(world, source)) {
            return false;
        } else {
            this.targetWorkGoal.cancel();
            return super.damage(world, source, amount);
        }
    }

    @Override
    protected Pair<Item, Integer> getLoot() {
        return new Pair<>(Items.PUMPKIN, 1);
    }

    @Override
    protected boolean isHealingItem(ItemStack itemStack) {
        return itemStack.isOf(Items.PUMPKIN) || itemStack.isOf(Items.PUMPKIN_SEEDS) || itemStack.isOf(Items.CARVED_PUMPKIN);
    }

    @Override
    protected int resetTicksUntilCanWork() {
        return MathHelper.nextInt(this.random, 9600, 12000);
    }

    @Override
    public void shootAt(LivingEntity target, float pullProgress) {
        double d = target.getX() - this.getX();
        double e = target.getEyeY() - 0.4F;
        double f = target.getZ() - this.getZ();
        double g = Math.sqrt(d * d + f * f) * (double)0.2F;
        World var12 = this.getWorld();
        if (var12 instanceof ServerWorld serverWorld) {
            ItemStack itemStack = new ItemStack(Items.PUMPKIN_SEEDS);
            ProjectileEntity.spawn(new SpitSeedProjectileEntity(serverWorld, this, itemStack), serverWorld, itemStack, (entity) -> entity.setVelocity(d, e + g - entity.getY(), f, 1.2F, 3.0F));
        }

        this.playSound(SoundEvents.ENTITY_LLAMA_SPIT, 1.0F, 0.4F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
    }


    class PumpkinTargetWorkGoal extends AbstractCropCritterEntity.TargetWorkGoal {
        @Override
        public void tick() {
            if (PumpkinCritterEntity.this.targetPos != null) {
                ++this.ticks;
                if (this.ticks > 600 || !(isAttractive(PumpkinCritterEntity.this.getWorld().getBlockState(PumpkinCritterEntity.this.targetPos)))) {
                    PumpkinCritterEntity.this.clearTargetPos();
                } else {
                    Vec3d vec3d = Vec3d.ofBottomCenter(PumpkinCritterEntity.this.targetPos).add(0.0F, getTargetOffset(), 0.0F);
                    if (vec3d.squaredDistanceTo(PumpkinCritterEntity.this.getPos()) > (double)10.0F) {
                        this.nextTarget = vec3d;
                        this.moveToNextTarget();
                    } else {
                        if (this.nextTarget == null) {
                            this.nextTarget = vec3d;
                        }
                        boolean bl = PumpkinCritterEntity.this.getPos().distanceTo(this.nextTarget) <= 5.0;
                        if (!bl && this.ticks > 600) {
                            PumpkinCritterEntity.this.clearTargetPos();
                        } else if (bl) {
                            // At target pos
                            PumpkinCritterEntity.this.completeTargetGoal();
                            PumpkinCritterEntity.this.clearTargetPos();
                        } else {
                            PumpkinCritterEntity.this.getMoveControl().moveTo(this.nextTarget.getX(), this.nextTarget.getY(), this.nextTarget.getZ(), 1.2F);
                        }
                    }
                }
            }
        }

        @Override
        protected Optional<BlockPos> getTargetBlock() {
            Iterable<BlockPos> iterable = BlockPos.iterateOutwards(PumpkinCritterEntity.this.getBlockPos(), 12, 2, 12);
            Long2LongOpenHashMap long2LongOpenHashMap = new Long2LongOpenHashMap();

            for(BlockPos blockPos : iterable) {
                long l = this.unreachableTargetsPosCache.getOrDefault(blockPos.asLong(), Long.MIN_VALUE);
                if (PumpkinCritterEntity.this.getWorld().getTime() < l) {
                    long2LongOpenHashMap.put(blockPos.asLong(), l);
                } else if (isAttractive(PumpkinCritterEntity.this.getWorld().getBlockState(blockPos)) && PumpkinCritterEntity.this.getWorld().getBlockState(blockPos.up()).isAir()) {
                    Path path = PumpkinCritterEntity.this.navigation.findPathTo(blockPos, 0);
                    if (path != null && path.reachesTarget()) {
                        return Optional.of(blockPos);
                    }

                    long2LongOpenHashMap.put(blockPos.asLong(), PumpkinCritterEntity.this.getWorld().getTime() + 600L);
                }
            }

            this.unreachableTargetsPosCache = long2LongOpenHashMap;
            return Optional.empty();
        }
    }

    class MelonActiveTargetGoal extends ActiveTargetGoal<LivingEntity> {

        public MelonActiveTargetGoal() {
            super(PumpkinCritterEntity.this, LivingEntity.class, 10, true, false, (entity, serverWorld) -> (!(entity.getType().isIn(CropCritters.CROP_CRITTERS))));
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
            return super.canStart() && !PumpkinCritterEntity.this.isTrusting();
        }

        @Override
        public boolean shouldContinue() {
            return super.shouldContinue() && !PumpkinCritterEntity.this.isTrusting();
        }
    }
}
