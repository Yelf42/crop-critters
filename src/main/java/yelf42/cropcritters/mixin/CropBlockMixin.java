package yelf42.cropcritters.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.event.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yelf42.cropcritters.blocks.ModBlocks;
import yelf42.cropcritters.config.ConfigManager;
import com.google.common.collect.ImmutableMap.Builder;
import yelf42.cropcritters.entity.AbstractCropCritterEntity;
import yelf42.cropcritters.entity.ModEntities;

import java.util.Map;
import java.util.Objects;

@Mixin(CropBlock.class)
public abstract class CropBlockMixin {

    // Allows crops to exist on SOUL_FARMLAND and Dirt's
    @Inject(method = "canPlantOnTop", at = @At("HEAD"), cancellable = true)
    private void allowPlantOnSoulFarmlandAndDirt(BlockState floor, BlockView world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (floor.isOf(ModBlocks.SOUL_FARMLAND) || floor.isIn(BlockTags.DIRT)) {
            cir.setReturnValue(true);
        }
    }

    // If SOUL_FARMLAND, ignore vanilla moisture stuff and just return 8.f
    @Inject(method = "getAvailableMoisture", at = @At("HEAD"), cancellable = true)
    private static void soulBasedMoisture(Block block, BlockView world, BlockPos pos, CallbackInfoReturnable<Float> cir) {
        BlockState blockState = world.getBlockState(pos.down());
        if (blockState.isOf(ModBlocks.SOUL_FARMLAND)) {
            cir.setReturnValue(18.f);
        }
    }

    // Inject into hasRandomTicks to make always true
    @Inject(method = "hasRandomTicks", at = @At("HEAD"), cancellable = true)
    private void overrideMatureStoppingRandomTicks(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }

    // Replace farmland with a dirt if just matured
    @Inject(method = "randomTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z", shift = At.Shift.AFTER))
    private static void removeNutrients(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
        if (state.getBlock() instanceof CropBlock cropBlock) {
            if (cropBlock.getAge(state) + 1 != cropBlock.getMaxAge()) return;
            BlockState soilCheck = world.getBlockState(pos.down());
            if (soilCheck.isOf(Blocks.FARMLAND)) return;
            BlockState toDirt = (random.nextInt(4) == 0) ? Blocks.DIRT.getDefaultState() : (random.nextInt(2) == 0) ? Blocks.ROOTED_DIRT.getDefaultState() : Blocks.COARSE_DIRT.getDefaultState();
            world.setBlockState(pos.down(), toDirt, Block.NOTIFY_LISTENERS);
        }
    }

    // Spawn critter chance if air above
    // Stop aging if not on farmland
    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    private static void stopGrowthOnDirtAndSpawnCritters(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
        if (state.getBlock() instanceof CropBlock cropBlock && cropBlock.isMature(state)) {
            BlockState airCheck = world.getBlockState(pos.up());
            if (airCheck.isAir() && random.nextInt(100) + 1 < ConfigManager.CONFIG.critter_spawn_chance) {
                if (world.getBlockState(pos).isOf(Blocks.WHEAT)) {
                    ModEntities.WHEAT_CRITTER.spawn(world, pos, SpawnReason.NATURAL);
                }
                world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
                // TODO Particles, SFX
                ci.cancel();
            }
        }

        BlockState soilCheck = world.getBlockState(pos.down());
        if (!(soilCheck.isOf(Blocks.FARMLAND) || soilCheck.isOf(ModBlocks.SOUL_FARMLAND))) ci.cancel();
    }

    // Inject into randomTicks to turn into weed if mature
    @Inject(method = "randomTick", at = @At("TAIL"))
    private static void injectWeedsIntoRandomTick(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
        if (state.getBlock() instanceof CropBlock cropBlock) {
            if (!cropBlock.isMature(state)) return;
        }

        // Count how many neighbours are the same type of crop
        // More identical crops increases chance of weed growth
        float monoCount = 1F;
        if (ConfigManager.CONFIG.monoculture_penalize) {
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    if (i == j && j == 0) continue;
                    BlockState cropToCheck = world.getBlockState(pos.add(i,0, j));
                    monoCount += cropToCheck.isOf(state.getBlock()) ? 1F : 0F;
                }
            }
            // Quadratic penalty increase for monocultural practices
            monoCount = (monoCount * monoCount) / (float)ConfigManager.CONFIG.monoculture_dampener;
        }
        boolean growThistle = random.nextInt(100) + 1 < (float)ConfigManager.CONFIG.thistle_chance * monoCount;
        boolean growThornweed = random.nextInt(100) + 1 < (float)ConfigManager.CONFIG.thornweed_chance * monoCount;
        boolean growSpiteweed = random.nextInt(100) + 1 < (float)ConfigManager.CONFIG.spiteweed_chance * monoCount;


        BlockState soilCheck = world.getBlockState(pos.down());
        if (Objects.equals(world.getBiome(pos).getIdAsString(), "minecraft:soul_sand_valley")) {
            if (growSpiteweed && (soilCheck.isOf(Blocks.SOUL_SOIL) || soilCheck.isOf(Blocks.SOUL_SAND) || soilCheck.isOf(ModBlocks.SOUL_FARMLAND))) {
                BlockState weedState = ModBlocks.WITHERING_SPITEWEED.getDefaultState();
                world.setBlockState(pos, weedState);
                world.setBlockState(pos.down(), Blocks.BLACKSTONE.getDefaultState(), Block.NOTIFY_LISTENERS);
                world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(null, weedState));
                return;
            }
        } else {
            if (growThistle && soilCheck.isOf(Blocks.FARMLAND)) {
                BlockState weedState = ModBlocks.CRAWL_THISTLE.getDefaultState();
                world.setBlockState(pos, weedState);
                world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(null, weedState));
                return;
            }

            if (growThornweed && soilCheck.isOf(ModBlocks.SOUL_FARMLAND)) {
                BlockState weedState = ModBlocks.CRIMSON_THORNWEED.getDefaultState();
                world.setBlockState(pos, weedState);
                world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(null, weedState));
                return;
            }
        }
    }
}
