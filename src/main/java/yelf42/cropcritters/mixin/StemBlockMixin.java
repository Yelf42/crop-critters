package yelf42.cropcritters.mixin;

import net.minecraft.block.*;
import net.minecraft.entity.SpawnReason;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.biome.BiomeKeys;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yelf42.cropcritters.blocks.ModBlocks;
import yelf42.cropcritters.config.ConfigManager;
import yelf42.cropcritters.entity.ModEntities;

import java.util.Optional;

@Mixin(StemBlock.class)
public abstract class StemBlockMixin {

    @Shadow @Final
    private RegistryKey<Block> gourdBlock;

    // Chance to spawn critter instead of make gourd
    @Inject(method = "randomTick", at = @At(value = "INVOKE", target = "Ljava/util/Optional;isPresent()Z", shift = At.Shift.AFTER), cancellable = true)
    private void spawnCritterOnGrow(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
        BlockState airCheck = world.getBlockState(pos.up());
        if (airCheck.isAir() && random.nextInt(100) + 1 < ConfigManager.CONFIG.critter_spawn_chance / 4F) {
            Registry<Block> registry = world.getRegistryManager().getOrThrow(RegistryKeys.BLOCK);
            Optional<Block> gourd = registry.getOptionalValue(this.gourdBlock);
            if (gourd.isPresent()) {
                if (spawnCritter(world, gourd.get().getDefaultState(), random, pos)) ci.cancel();
            }
        }
    }

    @Unique
    private static boolean spawnCritter(ServerWorld world, BlockState state, Random random, BlockPos pos) {
        boolean airCheck = world.getBlockState(pos.up()).isAir();
        if (!airCheck) return false;
        BlockState soil = world.getBlockState(pos.down());
        boolean soulCheck = soil.isOf(Blocks.SOUL_SOIL) || soil.isOf(Blocks.SOUL_SAND) || soil.isOf(ModBlocks.SOUL_FARMLAND);
        boolean soulSandValleyCheck = world.getBiome(pos).matchesKey(BiomeKeys.SOUL_SAND_VALLEY);
        int spawnChance = (ConfigManager.CONFIG.critter_spawn_chance / 4) * ((soulCheck) ? 2 : 1) * ((soulSandValleyCheck) ? 2 : 1);
        if (random.nextInt(100) + 1 < spawnChance) {
            if (state.isOf(Blocks.MELON)) {
                // TODO melon critter
            } else if (state.isOf(Blocks.PUMPKIN)) {
                // TODO pumpkin critter
            } else {
                return false;
            }
            world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
            // TODO Particles, SFX
            return true;
        }
        return false;
    }
}
