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
import yelf42.cropcritters.config.WeedPlacement;
import yelf42.cropcritters.entity.ModEntities;

import java.util.Objects;

import static net.minecraft.block.Block.pushEntitiesUpBeforeBlockChange;

@Mixin(NetherWartBlock.class)
public class NetherWartBlockMixin {
    @Shadow @Final public static IntProperty AGE;

    @Inject(method = "canPlantOnTop", at = @At("HEAD"), cancellable = true)
    private void allowPlantOnSoulStuff(BlockState floor, BlockView world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (floor.isOf(Blocks.SOUL_SAND) || floor.isOf(ModBlocks.SOUL_FARMLAND) || floor.isOf(Blocks.SOUL_SOIL)) {
            cir.setReturnValue(true);
            return;
        }
        cir.setReturnValue(false);
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

        if (spawnCritter(world, random, pos)) return;

        WeedPlacement.generateWeed(state, world, pos, random, true);
    }

    @Unique
    private static boolean spawnCritter(ServerWorld world, Random random, BlockPos pos) {
        boolean airCheck = world.getBlockState(pos.up()).isAir();
        int spawnChance = ConfigManager.CONFIG.critterSpawnChance * 2;
        if (world.getBiome(pos).matchesKey(BiomeKeys.SOUL_SAND_VALLEY)) spawnChance *= 2;

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
