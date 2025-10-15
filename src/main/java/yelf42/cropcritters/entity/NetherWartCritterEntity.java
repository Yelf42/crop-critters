package yelf42.cropcritters.entity;

import net.minecraft.block.*;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.EntityEffectParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;
import yelf42.cropcritters.blocks.ModBlocks;

import java.util.function.Predicate;

public class NetherWartCritterEntity extends AbstractCropCritterEntity {

    private static final EntityEffectParticleEffect PARTICLE_EFFECT = EntityEffectParticleEffect.create(ParticleTypes.ENTITY_EFFECT, ColorHelper.withAlpha(1F, 16073282));
    private static final int GO_CRAZY = 400;
    private static final TrackedData<Integer> LIFESPAN = DataTracker.registerData(NetherWartCritterEntity.class, TrackedDataHandlerRegistry.INTEGER);

    private static final ExplosionBehavior BURST = new ExplosionBehavior() {
        public boolean canDestroyBlock(Explosion explosion, BlockView world, BlockPos pos, BlockState state, float power) {
            return state.isOf(ModBlocks.WITHERING_SPITEWEED) || state.isOf(Blocks.WITHER_ROSE);
        }
        public boolean shouldDamage(Explosion explosion, Entity entity) {
            return false;
        }
    };


    public NetherWartCritterEntity(EntityType<? extends TameableEntity> entityType, World world) {
        super(entityType, world);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.MAX_HEALTH, 6)
                .add(EntityAttributes.MOVEMENT_SPEED, 0.25)
                .add(EntityAttributes.ATTACK_DAMAGE, 1)
                .add(EntityAttributes.FOLLOW_RANGE, 10)
                .add(EntityAttributes.TEMPT_RANGE, 10);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(LIFESPAN, 1200);
    }

    @Override
    protected Predicate<BlockState> getTargetBlockFilter() {
        return (blockState -> blockState.isOf(Blocks.SOUL_SOIL)
                                        || blockState.isOf((Blocks.SOUL_SAND))
                                        || blockState.isOf(ModBlocks.SOUL_FARMLAND)
                                        || blockState.isOf(Blocks.BLACKSTONE));
    }

    @Override
    public boolean isAttractive(BlockPos pos) {
        BlockState target = this.getWorld().getBlockState(pos);
        BlockState above = this.getWorld().getBlockState(pos.up());
        return this.getTargetBlockFilter().test(target) && (above.isAir() || (above.getBlock() instanceof PlantBlock && !above.contains(TallPlantBlock.HALF) && !(above.getBlock() instanceof NetherWartBlock)));
    }

    @Override
    protected  int getTargetOffset() {return 1;}

    @Override
    public void completeTargetGoal() {
        if (this.targetPos == null) return;
        BlockState target = this.getWorld().getBlockState(this.targetPos);
        if (target.isOf(Blocks.SOUL_SAND) || target.isOf(Blocks.SOUL_SOIL) || target.isOf(ModBlocks.SOUL_FARMLAND)) {
            this.playSound(SoundEvents.BLOCK_SOUL_SAND_PLACE, 1.0F, 1.0F);
            this.getWorld().setBlockState(this.targetPos.up(), Blocks.NETHER_WART.getDefaultState(), Block.NOTIFY_ALL_AND_REDRAW);
            ((ServerWorld)this.getWorld()).spawnParticles(ParticleTypes.HAPPY_VILLAGER, this.targetPos.getX() + 0.5, this.targetPos.getY() + 1.0, this.targetPos.getZ() + 0.5, 10, 0.5, 0.5, 0.5, 0.0);
            this.discard();
        } else {
            explode();
        }
    }

    @Override
    protected boolean isHealingItem(ItemStack itemStack) {
        return itemStack.isOf(Items.NETHER_WART);
    }

    @Override
    protected Pair<Item, Integer> getLoot() {
        return new Pair<>(Items.NETHER_WART, 3);
    }

    @Override
    protected int resetTicksUntilCanWork() {
        return 10;
    }

    @Override
    protected boolean canWork() {
        return !this.isTrusting();
    }

    @Override
    public boolean isShaking() {
        return this.dataTracker.get(LIFESPAN) < GO_CRAZY;
    }

    @Override
    protected void tryTame(PlayerEntity player) {
        if (!isShaking()) super.tryTame(null);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.getWorld().isClient) {
            if (!this.isTrusting()) this.dataTracker.set(LIFESPAN, this.dataTracker.get(LIFESPAN) - 1);
            if (this.dataTracker.get(LIFESPAN) <= 0) explode();
        } else if (this.isShaking()) {
            if (this.getWorld().random.nextInt(10) != 0) return;
            double x = this.getX() + (this.random.nextDouble() - 0.5) * this.getWidth();
            double y = this.getY() + this.getHeight() * 0.5;
            double z = this.getZ() + (this.random.nextDouble() - 0.5) * this.getWidth();
            this.getWorld().addParticleClient(PARTICLE_EFFECT, x, y, z, 0, 0, 0);
        }
    }

    private void explode() {
        if (this.getWorld() instanceof ServerWorld serverWorld) {
            Vec3d p = this.getPos();
            serverWorld.createExplosion(this, Explosion.createDamageSource(this.getWorld(), this), BURST, p.x, p.y, p.z, 1.5F, false, World.ExplosionSourceType.MOB, ParticleTypes.GUST_EMITTER_SMALL, ParticleTypes.GUST_EMITTER_LARGE, SoundEvents.ENTITY_BREEZE_WIND_BURST);
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    for (int k = -1; k <= 1; k++) {
                        BlockPos pos = this.getBlockPos().add(i, j, k);
                        if (serverWorld.getBlockState(pos).isOf(Blocks.BLACKSTONE)) {
                            serverWorld.setBlockState(pos, Blocks.SOUL_SAND.getDefaultState(), Block.NOTIFY_LISTENERS);
                        }
                        if (serverWorld.getBlockState(pos).isOf(ModBlocks.WITHERING_SPITEWEED)) {
                            this.getWorld().syncWorldEvent(this, 2001, this.targetPos, Block.getRawIdFromState(this.getWorld().getBlockState(this.targetPos)));
                            serverWorld.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
                        }
                    }
                }
            }
            this.dead = true;
            this.onRemoval(serverWorld, RemovalReason.KILLED);
            this.discard();
        }
    }
}
