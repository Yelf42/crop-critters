package yelf42.cropcritters.items;

import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.*;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import yelf42.cropcritters.entity.AbstractCropCritterEntity;
import yelf42.cropcritters.entity.ModEntities;

import java.util.Optional;

public class LostSoulItem extends Item {

    public LostSoulItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (context.getWorld().isClient) return ActionResult.PASS;
        ServerWorld world = (ServerWorld) context.getWorld();
        BlockPos blockPos = context.getBlockPos();
        BlockState state = world.getBlockState(blockPos);
        ItemStack itemStack = context.getStack();
        PlayerEntity playerEntity = context.getPlayer();

        // Create slime
        if (state.isOf(Blocks.SLIME_BLOCK)) {
            if (world.random.nextInt(2) == 0) {
                world.spawnParticles(ParticleTypes.SMOKE, blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5, 10, 0.5, 0.5, 0.5, 0F);
            } else {
                world.setBlockState(blockPos, Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
                SlimeEntity slime = EntityType.SLIME.create(world, SpawnReason.SPAWN_ITEM_USE);
                slime.setSize(2, true);
                slime.setPosition(blockPos.toBottomCenterPos());
                world.spawnEntity(slime);
                world.playSound(null, blockPos, SoundEvents.ENTITY_SLIME_SQUISH, SoundCategory.BLOCKS, 1F, 1F);
                world.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME, blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5, 10, 0.5, 0.5, 0.5, 0F);
            }
            if (playerEntity instanceof ServerPlayerEntity serverPlayerEntity) {
                Criteria.ITEM_USED_ON_BLOCK.trigger(serverPlayerEntity, blockPos, itemStack);
            }
            itemStack.decrement(1);
            return ActionResult.SUCCESS;
        }

        // Critter spawning logic
        Optional<AbstractCropCritterEntity> toSpawn = spawnCritter(world, blockPos, state);
        if (toSpawn.isEmpty()) return ActionResult.PASS;
        AbstractCropCritterEntity critter = toSpawn.get();
        int failChance = (critter.getMaxHealth() > 12) ? 80 : 60;
        if (world.random.nextInt(100) + 1 <= failChance) {
            world.spawnParticles(ParticleTypes.SMOKE, blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5, 10, 0.5, 0.5, 0.5, 0F);
        } else {
            BlockPos toSpawnAt = blockPos;
            if (state.get(PitcherCropBlock.HALF, DoubleBlockHalf.LOWER) == DoubleBlockHalf.UPPER) {
                world.setBlockState(blockPos.down(), Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
                toSpawnAt = blockPos.down();
            }
            world.setBlockState(blockPos, Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
            critter.setPosition(toSpawnAt.toBottomCenterPos());
            world.spawnEntity(critter);
            world.playSound(null, blockPos, SoundEvents.ENTITY_ALLAY_AMBIENT_WITH_ITEM, SoundCategory.BLOCKS, 1F, 1F);
            world.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME, blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5, 10, 0.5, 0.5, 0.5, 0F);
            if (playerEntity instanceof ServerPlayerEntity serverPlayerEntity) {
                Criteria.ITEM_USED_ON_BLOCK.trigger(serverPlayerEntity, blockPos, itemStack);
            }
            itemStack.decrement(1);
        }
        return ActionResult.SUCCESS;
    }

    private boolean spawnCritterOLD(ServerWorld world, BlockPos blockPos, BlockState state) {
        if (state.isOf(Blocks.PUMPKIN)) {
            ModEntities.PUMPKIN_CRITTER.spawn(world, blockPos, SpawnReason.NATURAL);
        } else if (state.isOf(Blocks.MELON)) {
            ModEntities.MELON_CRITTER.spawn(world, blockPos, SpawnReason.NATURAL);
        } else if (state.isOf(Blocks.COCOA) && state.get(CocoaBlock.AGE, 0) >= 2) {
            ModEntities.COCOA_CRITTER.spawn(world, blockPos, SpawnReason.NATURAL);
        } else if (state.getBlock() instanceof PlantBlock) {
            if (state.isOf(Blocks.PITCHER_PLANT) || (state.isOf(Blocks.PITCHER_CROP) && state.get(PitcherCropBlock.AGE, 0) >= 4)) {
                if (state.get(PitcherCropBlock.HALF, DoubleBlockHalf.LOWER) == DoubleBlockHalf.UPPER) blockPos = blockPos.down();
                ModEntities.PITCHER_CRITTER.spawn(world, blockPos, SpawnReason.NATURAL);
            } else if (state.isOf(Blocks.TORCHFLOWER)) {
                ModEntities.TORCHFLOWER_CRITTER.spawn(world, blockPos, SpawnReason.NATURAL);
            } else if (state.isOf(Blocks.NETHER_WART)) {
                for (int i = 0; i <= world.random.nextInt(3); i++) {
                    ModEntities.NETHER_WART_CRITTER.spawn(world, blockPos, SpawnReason.NATURAL);
                }
            } else if (state.getBlock() instanceof CropBlock cropBlock && cropBlock.isMature(state)) {
                if (state.isOf(Blocks.WHEAT)) {
                    ModEntities.WHEAT_CRITTER.spawn(world, blockPos, SpawnReason.NATURAL);
                } else if (state.isOf(Blocks.CARROTS)) {
                    ModEntities.CARROT_CRITTER.spawn(world, blockPos, SpawnReason.NATURAL);
                } else if (state.isOf(Blocks.POTATOES)) {
                    if (world.random.nextInt(100) + 1 < world.getDifficulty().getId() * 2) {
                        ModEntities.POISONOUS_POTATO_CRITTER.spawn(world, blockPos, SpawnReason.NATURAL);
                    } else {
                        ModEntities.POTATO_CRITTER.spawn(world, blockPos, SpawnReason.NATURAL);
                    }
                } else if (state.isOf(Blocks.BEETROOTS)) {
                    ModEntities.BEETROOT_CRITTER.spawn(world, blockPos, SpawnReason.NATURAL);
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
        return true;
    }

    private Optional<AbstractCropCritterEntity> spawnCritter(ServerWorld world, BlockPos blockPos, BlockState state) {
        AbstractCropCritterEntity output = null;
        if (state.isOf(Blocks.PUMPKIN)) {
            output = ModEntities.PUMPKIN_CRITTER.create(world, SpawnReason.SPAWN_ITEM_USE);
        } else if (state.isOf(Blocks.MELON)) {
            output = ModEntities.MELON_CRITTER.create(world, SpawnReason.SPAWN_ITEM_USE);
        } else if (state.isOf(Blocks.COCOA) && state.get(CocoaBlock.AGE, 0) >= 2) {
            output = ModEntities.COCOA_CRITTER.create(world, SpawnReason.SPAWN_ITEM_USE);
        } else if (state.getBlock() instanceof PlantBlock) {
            if (state.isOf(Blocks.PITCHER_PLANT) || (state.isOf(Blocks.PITCHER_CROP) && state.get(PitcherCropBlock.AGE, 0) >= 4)) {
                output = ModEntities.PITCHER_CRITTER.create(world, SpawnReason.SPAWN_ITEM_USE);
            } else if (state.isOf(Blocks.TORCHFLOWER)) {
                output = ModEntities.TORCHFLOWER_CRITTER.create(world, SpawnReason.SPAWN_ITEM_USE);
            } else if (state.isOf(Blocks.NETHER_WART)) {
                for (int i = 0; i <= world.random.nextInt(3); i++) {
                    output = ModEntities.NETHER_WART_CRITTER.create(world, SpawnReason.SPAWN_ITEM_USE);
                }
            } else if (state.getBlock() instanceof CropBlock cropBlock && cropBlock.isMature(state)) {
                if (state.isOf(Blocks.WHEAT)) {
                    output = ModEntities.WHEAT_CRITTER.create(world, SpawnReason.SPAWN_ITEM_USE);
                } else if (state.isOf(Blocks.CARROTS)) {
                    output = ModEntities.CARROT_CRITTER.create(world, SpawnReason.SPAWN_ITEM_USE);
                } else if (state.isOf(Blocks.POTATOES)) {
                    if (world.random.nextInt(100) + 1 < world.getDifficulty().getId() * 2) {
                        output = ModEntities.POISONOUS_POTATO_CRITTER.create(world, SpawnReason.SPAWN_ITEM_USE);
                    } else {
                        output = ModEntities.POTATO_CRITTER.create(world, SpawnReason.SPAWN_ITEM_USE);
                    }
                } else if (state.isOf(Blocks.BEETROOTS)) {
                    output = ModEntities.BEETROOT_CRITTER.create(world, SpawnReason.SPAWN_ITEM_USE);
                } else {
                    return Optional.empty();
                }
            } else {
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
        if (output == null) return Optional.empty();
        return Optional.of(output);
    }
}
