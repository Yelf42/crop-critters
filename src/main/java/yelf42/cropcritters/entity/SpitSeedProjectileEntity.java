package yelf42.cropcritters.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;

public class SpitSeedProjectileEntity extends ThrownItemEntity {

    public SpitSeedProjectileEntity(EntityType<? extends ThrownItemEntity> entityType, World world) {
        super(entityType, world);
    }

    public SpitSeedProjectileEntity(double x, double y, double z, World world, ItemStack stack) {
        super(ModEntities.SPIT_SEED_PROJECTILE, x, y, z, world, stack);
        //this.spatItem = stack.getItem();
    }

    public SpitSeedProjectileEntity(ServerWorld serverWorld, LivingEntity livingEntity, ItemStack itemStack) {
        super(ModEntities.SPIT_SEED_PROJECTILE, livingEntity, serverWorld, itemStack);
        //this.spatItem = itemStack.getItem();
    }

    @Override
    protected Item getDefaultItem() {
        return Items.PUMPKIN_SEEDS;
    }

    @Override
    public void tick() {
        super.tick();
        this.getWorld().sendEntityStatus(this, (byte)4);
    }

    @Environment(EnvType.CLIENT)
    private ParticleEffect getParticleParameters() {
        ItemStack itemStack = this.getStack();
        return ParticleTypes.SPLASH;
    }

    @Environment(EnvType.CLIENT)
    public void handleStatus(byte status) {
        ParticleEffect particleEffect = this.getParticleParameters();
        if (status == 3) {
            for(int i = 0; i < 8; ++i) {
                this.getWorld().addParticleClient(particleEffect, this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
            }
        }
        if (status == 4) {
            this.getWorld().addParticleClient(particleEffect, this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
        }
    }



    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) { // called on entity hit.
        super.onEntityHit(entityHitResult);
        Entity entity = entityHitResult.getEntity(); // sets a new Entity instance as the EntityHitResult (victim)
        entity.serverDamage(this.getDamageSources().thrown(this, this.getOwner()), 1F);
        if (!this.getWorld().isClient) {
            this.getWorld().sendEntityStatus(this, (byte)3);
            this.discard();
        }
    }

    @Override
    protected void onBlockCollision(BlockState state) {
        World world = this.getWorld();
        if (!world.isClient) {
            world.sendEntityStatus(this, (byte)3);
            this.discard();
        }
    }
}
