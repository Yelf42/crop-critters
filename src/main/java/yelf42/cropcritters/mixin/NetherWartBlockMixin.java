package yelf42.cropcritters.mixin;

import net.minecraft.block.*;
import net.minecraft.entity.SpawnReason;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
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

import static net.minecraft.block.Block.pushEntitiesUpBeforeBlockChange;

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
    @Inject(method = "randomTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z", shift = At.Shift.AFTER))
    private static void spawnCritterOnJustMatured(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
        int wartAge = state.get(AGE, 0);
        if ((wartAge >= NetherWartBlock.MAX_AGE) && spawnCritter(world, random, pos)) return;
    }

    // Inject into randomTicks to turn into weed if mature
    // Grow faster on soul farmland
    @Inject(method = "randomTick", at = @At("TAIL"))
    private static void injectIntoRandomTicksTail(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
        // Grow faster on soul farmland
        BlockState soilCheck = world.getBlockState(pos.down());
        int wartAge = state.get(AGE, 0);
        if (wartAge < NetherWartBlock.MAX_AGE) {
            if (soilCheck.isOf(ModBlocks.SOUL_FARMLAND) && (random.nextInt(7) == 0)) {
                state = state.with(AGE, wartAge + 1);
                world.setBlockState(pos, state, 2);
            }
            return;
        }

        // Chance to spawn critter if in SoulSandValley
        if (world.getBiome(pos).matchesKey(BiomeKeys.SOUL_SAND_VALLEY)) {
            if (spawnCritter(world, random, pos)) return;
        }

        // Count how many neighbours are the same type of crop
        // More identical crops increases chance of weed growth
        float monoCount = 1F;
        if (ConfigManager.CONFIG.monoculturePenalize) {
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    if (i == j && j == 0) continue;
                    BlockState cropToCheck = world.getBlockState(pos.add(i,0, j));
                    monoCount += cropToCheck.isOf(state.getBlock()) ? 1F : 0F;
                }
            }
            // Quadratic penalty increase for monocultural practices
            monoCount = (monoCount * monoCount) / 16F;
        }
        boolean growNetherWeed = random.nextInt(100) + 1 < (float)ConfigManager.CONFIG.netherWeedChance * (monoCount + 1);
        boolean growSpiteweed = random.nextInt(100) + 1 < (float)ConfigManager.CONFIG.spiteweedChance * (monoCount + 1);

        int weedTypeCheck = random.nextInt(100) + 1;

        if (world.getBiome(pos).matchesKey(BiomeKeys.SOUL_SAND_VALLEY)) {
            if (growSpiteweed) {
                BlockState weedState = ModBlocks.WITHERING_SPITEWEED.getDefaultState();
                world.setBlockState(pos, weedState);
                pushEntitiesUpBeforeBlockChange(Blocks.SOUL_SAND.getDefaultState(), Blocks.BLACKSTONE.getDefaultState(), world, pos.down());
                world.setBlockState(pos.down(), Blocks.BLACKSTONE.getDefaultState(), Block.NOTIFY_LISTENERS);
                world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(null, weedState));
                return;
            }
        } else if (growNetherWeed) {
            BlockState weedState = ModBlocks.CRIMSON_THORNWEED.getDefaultState();
            // Add further nether weeds here
            if (weedTypeCheck < 20) {
                weedState = ModBlocks.WAFTGRASS.getDefaultState();
            }
            world.setBlockState(pos, weedState);
            pushEntitiesUpBeforeBlockChange(ModBlocks.SOUL_FARMLAND.getDefaultState(), Blocks.SOUL_SOIL.getDefaultState(), world, pos.down());
            world.setBlockState(pos.down(), (random.nextInt(2) == 0) ? Blocks.SOUL_SOIL.getDefaultState() : Blocks.SOUL_SAND.getDefaultState(), Block.NOTIFY_LISTENERS);
            world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(null, weedState));
            return;
        }
    }

    @Unique
    private static boolean spawnCritter(ServerWorld world, Random random, BlockPos pos) {
        boolean airCheck = world.getBlockState(pos.up()).isAir();
        int spawnChance = ConfigManager.CONFIG.critterSpawnChance * 2;
        if (airCheck && random.nextInt(100) + 1 < spawnChance) {
            for (int i = 0; i <= world.random.nextInt(3); i++) {
                ModEntities.NETHER_WART_CRITTER.spawn(world, pos, SpawnReason.NATURAL);
            }
            world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
            world.playSound(null, pos, SoundEvents.ENTITY_ALLAY_AMBIENT_WITH_ITEM, SoundCategory.BLOCKS, 1F, 1F);
            world.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 10, 0.5, 0.5, 0.5, 0F);
            return true;
        }
        return false;
    }
}
