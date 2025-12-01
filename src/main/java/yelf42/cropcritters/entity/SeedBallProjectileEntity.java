package yelf42.cropcritters.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import yelf42.cropcritters.CropCritters;
import yelf42.cropcritters.items.ModComponents;
import yelf42.cropcritters.items.ModItems;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SeedBallProjectileEntity extends ThrownItemEntity {

    private static final List<Identifier> DefaultSeedTypes = Arrays.asList(Registries.BLOCK.getId(Blocks.WHEAT), Registries.BLOCK.getId(Blocks.CARROTS), Registries.BLOCK.getId(Blocks.POTATOES), Registries.BLOCK.getId(Blocks.BEETROOTS));


    SeedBallProjectileEntity(EntityType<? extends ThrownItemEntity> entityType, World world) {
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
        return (itemStack.isEmpty() ? ParticleTypes.SPLASH : new ItemStackParticleEffect(ParticleTypes.ITEM, itemStack));
    }

    @Environment(EnvType.CLIENT)
    public void handleStatus(byte status) {
        if (status == 3) {
            ParticleEffect particleEffect = this.getParticleParameters();
            for(int i = 0; i < 8; ++i) {
                this.getEntityWorld().addParticleClient(particleEffect, this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
            }
        }
    }



    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        Entity entity = entityHitResult.getEntity();
        if (entity instanceof PlayerEntity player) player.addStatusEffect((new StatusEffectInstance(StatusEffects.BLINDNESS, 20 * 4, 0)));
        if (entity instanceof LivingEntity livingEntity) {
            int p = this.getStack().getOrDefault(ModComponents.POISONOUS_SEED_BALL, new ModComponents.PoisonousComponent(0)).poisonStacks();
            livingEntity.addStatusEffect((new StatusEffectInstance(StatusEffects.POISON, 20 * 6 * p, 0)));
        }
        if (!this.getEntityWorld().isClient()) {
            this.getEntityWorld().sendEntityStatus(this, (byte)3);
            this.discard();
        }
    }

    @Override
    protected void onBlockCollision(BlockState state) {
        World world = this.getEntityWorld();
        if (!world.isClient()) {
            if (!state.isSolid()) return;
            if (!state.isIn(BlockTags.DIRT)) {
                this.discard();
                return;
            }

            List<Identifier> crops = this.getStack().getOrDefault(ModComponents.SEED_TYPES, new ModComponents.SeedTypesComponent(DefaultSeedTypes)).seedTypes();
            if (crops.isEmpty()) {
                this.discard();
                return;
            }

            Iterable<BlockPos> iterable = BlockPos.iterateOutwards(this.getBlockPos(), 2, 3, 2);
            for(BlockPos blockPos : iterable) {
                BlockState blockState = world.getBlockState(blockPos);
                blockState = Registries.BLOCK.get(crops.get(this.random.nextInt(crops.size()))).getDefaultState();
                if ((world.random.nextInt(2) == 0 || blockPos == this.getBlockPos()) && blockState.canPlaceAt(world, blockPos) && world.getBlockState(blockPos).isAir()) {
                    world.setBlockState(blockPos, blockState);
                }
            }
            world.sendEntityStatus(this, (byte)3);
            this.discard();
        }
    }
}
