package yelf42.cropcritters.config;

import net.minecraft.block.*;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.SpawnReason;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import yelf42.cropcritters.blocks.ModBlocks;
import yelf42.cropcritters.entity.AbstractCropCritterEntity;
import yelf42.cropcritters.entity.ModEntities;

import java.util.Optional;

public class CritterHelper {

    public static Optional<AbstractCropCritterEntity> spawnCritterWithItem(ServerWorld world, BlockState state) {
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
                    if (world.random.nextInt(100) + 1 < world.getDifficulty().getId() * 5) {
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


    public static boolean spawnCritter(ServerWorld world, BlockState state, Random random, BlockPos pos) {
        boolean bottomHalf = state.get(TallPlantBlock.HALF, DoubleBlockHalf.LOWER) == DoubleBlockHalf.LOWER;
        if (!bottomHalf) return false;

        BlockState soulCheck = world.getBlockState(pos.down());
        boolean soulCheckBl = soulCheck.isOf(Blocks.SOUL_SOIL) || soulCheck.isOf(Blocks.SOUL_SAND) || soulCheck.isOf(ModBlocks.SOUL_FARMLAND);
        boolean airCheck = world.getBlockState(pos.up()).isAir();
        int spawnChance = ConfigManager.CONFIG.critterSpawnChance;
        spawnChance *= (soulCheckBl) ? 2 : 1;
        if (airCheck && random.nextInt(100) + 1 < spawnChance) {
            if (state.isOf(Blocks.WHEAT)) {
                ModEntities.WHEAT_CRITTER.spawn(world, pos, SpawnReason.NATURAL);
            } else if (state.isOf(Blocks.CARROTS)) {
                ModEntities.CARROT_CRITTER.spawn(world, pos, SpawnReason.NATURAL);
            } else if (state.isOf(Blocks.POTATOES)) {
                if (random.nextInt(100) + 1 < world.getDifficulty().getId() * 5) {
                    ModEntities.POISONOUS_POTATO_CRITTER.spawn(world, pos, SpawnReason.NATURAL);
                } else {
                    ModEntities.POTATO_CRITTER.spawn(world, pos, SpawnReason.NATURAL);
                }
            } else if (state.isOf(Blocks.NETHER_WART)) {
                for (int i = 0; i <= world.random.nextInt(3); i++) {
                    ModEntities.NETHER_WART_CRITTER.spawn(world, pos, SpawnReason.NATURAL);
                }
            } else if (state.isOf(Blocks.BEETROOTS)) {
                ModEntities.BEETROOT_CRITTER.spawn(world, pos, SpawnReason.NATURAL);
            } else if (state.isOf(Blocks.NETHER_WART)) {
                for (int i = 0; i <= world.random.nextInt(3); i++) {
                    ModEntities.NETHER_WART_CRITTER.spawn(world, pos, SpawnReason.NATURAL);
                }
            } else if (state.isOf(Blocks.TORCHFLOWER_CROP)) {
                ModEntities.TORCHFLOWER_CRITTER.spawn(world, pos, SpawnReason.NATURAL);
            } else if (state.isOf(Blocks.PITCHER_CROP)) {
                ModEntities.PITCHER_CRITTER.spawn(world, pos, SpawnReason.NATURAL);
            }  else {
                return false;
            }
            world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
            world.playSound(null, pos, SoundEvents.ENTITY_ALLAY_AMBIENT_WITH_ITEM, SoundCategory.BLOCKS, 1F, 1F);
            world.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 10, 0.5, 0.5, 0.5, 0F);

            return true;
        }
        return false;
    }

}
