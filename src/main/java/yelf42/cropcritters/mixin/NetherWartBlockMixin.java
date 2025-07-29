package yelf42.cropcritters.mixin;

import net.minecraft.block.*;
import net.minecraft.entity.SpawnReason;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.event.GameEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yelf42.cropcritters.blocks.ModBlocks;
import yelf42.cropcritters.config.ConfigManager;
import yelf42.cropcritters.entity.ModEntities;

import java.util.Objects;

@Mixin(NetherWartBlock.class)
public class NetherWartBlockMixin {
    @Shadow @Final public static IntProperty AGE;

    @Inject(method = "canPlantOnTop", at = @At("HEAD"), cancellable = true)
    private void allowPlantOnSoulStuff(BlockState floor, BlockView world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (floor.isOf(ModBlocks.SOUL_FARMLAND) || floor.isOf(Blocks.SOUL_SOIL)) {
            cir.setReturnValue(true);
        }
    }

    // Inject into randomTicks for chance to spawn critter on just matured
    @Inject(method = "randomTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z", shift = At.Shift.AFTER), cancellable = true)
    private static void spawnCritterOnJustMatured(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
        int wartAge = state.get(AGE, 0);
        if ((wartAge >= 3) && spawnCritter(world, random, pos)) ci.cancel();
    }

        // Inject into randomTicks to turn into weed if mature
    // Grow faster on soul farmland
    @Inject(method = "randomTick", at = @At("TAIL"), cancellable = true)
    private static void injectIntoRandomTicksTail(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
        // Grow faster on soul farmland
        BlockState soilCheck = world.getBlockState(pos.down());
        int wartAge = state.get(AGE, 0);
        if (wartAge < 3) {
            if (soilCheck.isOf(ModBlocks.SOUL_FARMLAND) && (random.nextInt(7) == 0)) {
                state = state.with(AGE, wartAge + 1);
                world.setBlockState(pos, state, 2);
            }
            return;
        }

        // Chance to spawn critter if in SoulSandValley
        if (world.getBiome(pos).matchesKey(BiomeKeys.SOUL_SAND_VALLEY)) {
            if (spawnCritter(world, random, pos)) ci.cancel();
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
        boolean growWaftgrass = random.nextInt(100) + 1 < (float)ConfigManager.CONFIG.waftgrass_chance * monoCount;
        boolean growThornweed = random.nextInt(100) + 1 < (float)ConfigManager.CONFIG.thornweed_chance * monoCount;
        boolean growSpiteweed = random.nextInt(100) + 1 < (float)ConfigManager.CONFIG.spiteweed_chance * monoCount;

        if (Objects.equals(world.getBiome(pos).getIdAsString(), "minecraft:soul_sand_valley")) {
            if (growSpiteweed && (soilCheck.isOf(Blocks.SOUL_SOIL) || soilCheck.isOf(Blocks.SOUL_SAND) || soilCheck.isOf(ModBlocks.SOUL_FARMLAND))) {
                BlockState weedState = ModBlocks.WITHERING_SPITEWEED.getDefaultState();
                world.setBlockState(pos, weedState);
                world.setBlockState(pos.down(), Blocks.BLACKSTONE.getDefaultState(), Block.NOTIFY_LISTENERS);
                world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(null, weedState));
                return;
            }
        } else {
            if (growThornweed) {
                BlockState weedState = ModBlocks.CRIMSON_THORNWEED.getDefaultState();
                world.setBlockState(pos, weedState);
                world.setBlockState(pos.down(),(random.nextInt(2) == 0) ? Blocks.SOUL_SOIL.getDefaultState() : Blocks.SOUL_SAND.getDefaultState(), Block.NOTIFY_LISTENERS);
                world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(null, weedState));
                return;
            }

            if (growWaftgrass) {
                BlockState weedState = ModBlocks.WAFTGRASS.getDefaultState();
                world.setBlockState(pos, weedState);
                world.setBlockState(pos.down(),(random.nextInt(2) == 0) ? Blocks.SOUL_SOIL.getDefaultState() : Blocks.SOUL_SAND.getDefaultState(), Block.NOTIFY_LISTENERS);
                world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(null, weedState));
                return;
            }
        }
    }

    @Unique
    private static boolean spawnCritter(ServerWorld world, Random random, BlockPos pos) {
        boolean airCheck = world.getBlockState(pos.up()).isAir();
        int spawnChance = ConfigManager.CONFIG.critter_spawn_chance * 2;
        if (airCheck && random.nextInt(100) + 1 < spawnChance) {
            // TODO spawn netherwart critter
            world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
            // TODO Particles, SFX
            return true;
        }
        return false;
    }
}
