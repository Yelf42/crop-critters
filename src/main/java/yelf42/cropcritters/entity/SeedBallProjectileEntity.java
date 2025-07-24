package yelf42.cropcritters.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import yelf42.cropcritters.CropCritters;
import yelf42.cropcritters.items.ModItems;

public class SeedBallProjectileEntity extends ThrownItemEntity {
    public SeedBallProjectileEntity(EntityType<? extends ThrownItemEntity> entityType, World world) {
        super(entityType, world);
    }

    public SeedBallProjectileEntity(double x, double y, double z, World world, ItemStack stack) {
        super(ModEntities.SEED_BALL_PROJECTILE, x, y, z, world, stack);
    }

    public SeedBallProjectileEntity(ServerWorld serverWorld, LivingEntity livingEntity, ItemStack itemStack) {
        super(ModEntities.SEED_BALL_PROJECTILE, livingEntity, serverWorld, itemStack);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.SEED_BALL;
    }

    @Environment(EnvType.CLIENT)
    private ParticleEffect getParticleParameters() {
        ItemStack itemStack = this.getStack();
        return (ParticleEffect)(itemStack.isEmpty() ? ParticleTypes.SPLASH : new ItemStackParticleEffect(ParticleTypes.ITEM, itemStack));
    }

    @Environment(EnvType.CLIENT)
    public void handleStatus(byte status) {
        if (status == 3) {
            ParticleEffect particleEffect = this.getParticleParameters();
            for(int i = 0; i < 8; ++i) {
                this.getWorld().addParticleClient(particleEffect, this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
            }
        }
    }



    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) { // called on entity hit.
        super.onEntityHit(entityHitResult);
        Entity entity = entityHitResult.getEntity(); // sets a new Entity instance as the EntityHitResult (victim)
        entity.serverDamage(this.getDamageSources().thrown(this, this.getOwner()), 3F);
        if (entity instanceof PlayerEntity player) { // checks if entity is an instance of LivingEntity (meaning it is not a boat or minecart)
            player.addStatusEffect((new StatusEffectInstance(StatusEffects.BLINDNESS, 20 * 3, 0)));
        }
        if (!this.getWorld().isClient) {
            this.getWorld().sendEntityStatus(this, (byte)3);
            this.discard();
        }
    }

    @Override
    protected void onBlockCollision(BlockState state) {
        World world = this.getWorld();
        Iterable<BlockPos> iterable = BlockPos.iterateOutwards(this.getBlockPos(), 2, 3, 2);
        for(BlockPos blockPos : iterable) {
            BlockState blockState = world.getBlockState(blockPos);
            blockState = (BlockState) Registries.BLOCK
                    .getRandomEntry(CropCritters.SEED_BALL_CROPS, world.random)
                    .map(blockEntry -> ((Block)blockEntry.value()).getDefaultState())
                    .orElse(blockState);
            if (world.random.nextInt(2) == 0 && blockState.canPlaceAt(world, blockPos) && world.getBlockState(blockPos).isAir()) {
                world.setBlockState(blockPos, blockState);
            }
        }
            super.onBlockCollision(state);
        if (!world.isClient) {
            world.sendEntityStatus(this, (byte)3);
            this.discard();
        }
    }
}
