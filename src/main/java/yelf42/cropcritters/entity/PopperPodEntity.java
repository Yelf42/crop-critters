package yelf42.cropcritters.entity;

import it.unimi.dsi.fastutil.doubles.DoubleDoubleImmutablePair;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jspecify.annotations.Nullable;
import yelf42.cropcritters.blocks.ModBlocks;
import yelf42.cropcritters.items.ModItems;
import yelf42.cropcritters.sound.ModSounds;

import java.util.List;
import java.util.OptionalInt;

public class PopperPodEntity extends ProjectileEntity implements FlyingItemEntity {
    private static final TrackedData<ItemStack> ITEM;
    private static final TrackedData<OptionalInt> SHOOTER_ENTITY_ID;
    private static final TrackedData<Boolean> SHOT_AT_ANGLE;
    private int life;
    private int lifeTime;
    private boolean shouldExplode = true;
    private @Nullable LivingEntity shooter;

    public PopperPodEntity(EntityType<? extends PopperPodEntity> entityType, World world) {
        super(entityType, world);
        this.life = 0;
        this.lifeTime = 0;
    }

    public PopperPodEntity(World world, double x, double y, double z, ItemStack stack) {
        super(ModEntities.POPPER_POD_PROJECTILE, world);
        this.life = 0;
        this.lifeTime = 0;
        this.life = 0;
        this.setPosition(x, y, z);
        this.dataTracker.set(ITEM, stack.copy());
        this.setVelocity(this.random.nextTriangular((double)0.0F, 0.002297), 0.05, this.random.nextTriangular((double)0.0F, 0.002297));
        this.lifeTime = 15 + this.random.nextInt(6) + this.random.nextInt(7);
    }

    public PopperPodEntity(World world, @Nullable Entity entity, double x, double y, double z, ItemStack stack) {
        this(world, x, y, z, stack);
        this.setOwner(entity);
    }

    public PopperPodEntity(World world, ItemStack stack, LivingEntity shooter) {
        this(world, shooter, shooter.getX(), shooter.getY(), shooter.getZ(), stack);
        this.dataTracker.set(SHOOTER_ENTITY_ID, OptionalInt.of(shooter.getId()));
        this.shooter = shooter;
        this.shouldExplode = false;
    }

    public PopperPodEntity(World world, ItemStack stack, double x, double y, double z, boolean shotAtAngle) {
        this(world, x, y, z, stack);
        this.dataTracker.set(SHOT_AT_ANGLE, shotAtAngle);
    }

    protected void initDataTracker(DataTracker.Builder builder) {
        builder.add(ITEM, getDefaultStack());
        builder.add(SHOOTER_ENTITY_ID, OptionalInt.empty());
        builder.add(SHOT_AT_ANGLE, false);
    }

    public boolean shouldRender(double distance) {
        return distance < (double)4096.0F && !this.wasShotByEntity();
    }

    public boolean shouldRender(double cameraX, double cameraY, double cameraZ) {
        return super.shouldRender(cameraX, cameraY, cameraZ) && !this.wasShotByEntity();
    }

    public void tick() {
        super.tick();
        HitResult hitResult;
        if (this.wasShotByEntity()) {
            if (this.shooter == null) {
                ((OptionalInt)this.dataTracker.get(SHOOTER_ENTITY_ID)).ifPresent((id) -> {
                    Entity entity = this.getEntityWorld().getEntityById(id);
                    if (entity instanceof LivingEntity) {
                        this.shooter = (LivingEntity)entity;
                    }

                });
            }

            if (this.shooter != null) {
                Vec3d vec3d3;
                if (this.shooter.isGliding()) {
                    Vec3d vec3d = this.shooter.getRotationVector();
                    double d = (double)1.5F;
                    double e = 0.1;
                    Vec3d vec3d2 = this.shooter.getVelocity();
                    this.shooter.setVelocity(vec3d2.add(vec3d.x * 0.1 + (vec3d.x * (double)1.5F - vec3d2.x) * (double)0.5F, vec3d.y * 0.1 + (vec3d.y * (double)1.5F - vec3d2.y) * (double)0.5F, vec3d.z * 0.1 + (vec3d.z * (double)1.5F - vec3d2.z) * (double)0.5F));
                    vec3d3 = this.shooter.getHandPosOffset(ModItems.POPPER_POD);
                } else {
                    vec3d3 = Vec3d.ZERO;
                }

                this.setPosition(this.shooter.getX() + vec3d3.x, this.shooter.getY() + vec3d3.y, this.shooter.getZ() + vec3d3.z);
                this.setVelocity(this.shooter.getVelocity());
            }

            hitResult = ProjectileUtil.getCollision(this, this::canHit);
        } else {
            if (!this.wasShotAtAngle()) {
                double f = this.horizontalCollision ? (double)1.0F : 1.15;
                this.setVelocity(this.getVelocity().multiply(f, (double)1.0F, f).add((double)0.0F, 0.04, (double)0.0F));
            }

            Vec3d vec3d3 = this.getVelocity();
            hitResult = ProjectileUtil.getCollision(this, this::canHit);
            this.move(MovementType.SELF, vec3d3);
            this.tickBlockCollision();
            this.setVelocity(vec3d3);
        }

        if (!this.noClip && this.isAlive() && hitResult.getType() != HitResult.Type.MISS) {
            this.hitOrDeflect(hitResult);
            this.velocityDirty = true;
        }

        this.updateRotation();
        if (this.life == 0 && !this.isSilent()) {
            // Launch sfx
            this.getEntityWorld().playSound(null, this.getX(), this.getY(), this.getZ(), ModSounds.POPPER_POD_LAUNCH, SoundCategory.AMBIENT, 2.0F, 1.0F + (random.nextFloat() * 0.8F - 0.4F));
        }

        ++this.life;
        if (this.getEntityWorld().isClient()) {
            this.getEntityWorld().addParticleClient(ParticleTypes.SPLASH, this.getX(), this.getY(), this.getZ(), this.random.nextGaussian() * 0.05, -this.getVelocity().y * (double)0.5F, this.random.nextGaussian() * 0.05);
        }

        if (this.life > this.lifeTime) {
            World var12 = this.getEntityWorld();
            if (var12 instanceof ServerWorld serverWorld) {
                this.explodeAndRemove(serverWorld);
            }
        }

    }

    private void explodeAndRemove(ServerWorld world) {
        world.sendEntityStatus(this, (byte)17);
        this.emitGameEvent(GameEvent.EXPLODE, this.getOwner()); // IDK what this does
        this.getEntityWorld().playSound((Entity)null, this.getX(), this.getY(), this.getZ(), ModSounds.POPPER_POD_POP, SoundCategory.AMBIENT, 2.0F, 1.0F + (random.nextFloat() * 0.8F - 0.4F));
        if (this.shouldExplode) this.explode(world);
        this.discard();
    }

    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
        World var3 = this.getEntityWorld();
        if (var3 instanceof ServerWorld serverWorld) {
            this.explodeAndRemove(serverWorld);
        }

    }

    protected void onBlockHit(BlockHitResult blockHitResult) {
        BlockPos blockPos = new BlockPos(blockHitResult.getBlockPos());
        this.getEntityWorld().getBlockState(blockPos).onEntityCollision(this.getEntityWorld(), blockPos, this, EntityCollisionHandler.DUMMY, true);
        World var4 = this.getEntityWorld();
        if (var4 instanceof ServerWorld serverWorld) {
            this.explodeAndRemove(serverWorld);
        }

        super.onBlockHit(blockHitResult);
    }

    private void explode(ServerWorld world) {
        int count = (random.nextInt(3) != 0) ? 1 : (random.nextInt(3) != 0 ? 2 : 3);

        for (int i = 0; i < count; i++) {
            double angle = random.nextDouble() * Math.PI * 2.0f;
            ProjectileEntity.spawn(new PopperSeedEntity(this.getEntityPos(), world),
                    world,
                    new ItemStack(ModBlocks.POPPER_PLANT.asItem()),
                    (entity) -> entity.setVelocity(Math.sin(angle), 0.0f, Math.cos(angle), random.nextFloat() * 0.3f + 0.1f, 10.0F));
        }
    }

    private boolean wasShotByEntity() {
        return ((OptionalInt)this.dataTracker.get(SHOOTER_ENTITY_ID)).isPresent();
    }

    public boolean wasShotAtAngle() {
        return (Boolean)this.dataTracker.get(SHOT_AT_ANGLE);
    }

    public void handleStatus(byte status) {
        if (status == 17 && this.getEntityWorld().isClient()) {
            Vec3d vec3d = this.getVelocity();
            this.getEntityWorld().addFireworkParticle(this.getX(), this.getY(), this.getZ(), vec3d.x, vec3d.y, vec3d.z, List.of());
        }
        super.handleStatus(status);
    }

    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        view.putInt("Life", this.life);
        view.putInt("LifeTime", this.lifeTime);
        view.put("PopperPodItem", ItemStack.CODEC, this.getStack());
        view.putBoolean("ShotAtAngle", (Boolean)this.dataTracker.get(SHOT_AT_ANGLE));
    }

    protected void readCustomData(ReadView view) {
        super.readCustomData(view);
        this.life = view.getInt("Life", 0);
        this.lifeTime = view.getInt("LifeTime", 0);
        this.dataTracker.set(ITEM, (ItemStack)view.read("PopperPodItem", ItemStack.CODEC).orElse(getDefaultStack()));
        this.dataTracker.set(SHOT_AT_ANGLE, view.getBoolean("ShotAtAngle", false));
    }

    public ItemStack getStack() {
        return (ItemStack)this.dataTracker.get(ITEM);
    }

    public boolean isAttackable() {
        return false;
    }

    private static ItemStack getDefaultStack() {
        return new ItemStack(ModItems.POPPER_POD);
    }

    public DoubleDoubleImmutablePair getKnockback(LivingEntity target, DamageSource source) {
        double d = target.getEntityPos().x - this.getEntityPos().x;
        double e = target.getEntityPos().z - this.getEntityPos().z;
        return DoubleDoubleImmutablePair.of(d, e);
    }

    static {
        ITEM = DataTracker.registerData(PopperPodEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);
        SHOOTER_ENTITY_ID = DataTracker.registerData(PopperPodEntity.class, TrackedDataHandlerRegistry.OPTIONAL_INT);
        SHOT_AT_ANGLE = DataTracker.registerData(PopperPodEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    }
}
