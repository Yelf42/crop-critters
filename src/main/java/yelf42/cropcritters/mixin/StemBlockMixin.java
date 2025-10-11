package yelf42.cropcritters.mixin;

import net.minecraft.block.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yelf42.cropcritters.blocks.ModBlocks;

@Mixin(StemBlock.class)
public abstract class StemBlockMixin {

    // Allow planting on same blocks as other crops
    @Inject(method = "canPlantOnTop", at = @At("HEAD"), cancellable = true)
    private void allowPlantOnSoulAndDirt(BlockState floor, BlockView world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (floor.isOf(ModBlocks.SOUL_FARMLAND)) cir.setReturnValue(true);
    }

    // Chance to spawn critter instead of make gourd
//    @Inject(method = "randomTick", at = @At(value = "INVOKE", target = "Ljava/util/Optional;isPresent()Z", shift = At.Shift.AFTER), cancellable = true)
//    private void spawnCritterOnGrow(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
//        BlockState airCheck = world.getBlockState(pos.up());
//        if (airCheck.isAir()) {
//            BlockState soil = world.getBlockState(pos.down());
//            boolean soulCheck = soil.isOf(ModBlocks.SOUL_FARMLAND);
//            boolean soulSandValleyCheck = world.getBiome(pos).matchesKey(BiomeKeys.SOUL_SAND_VALLEY);
//            int spawnChance = (ConfigManager.CONFIG.critterSpawnChance / 4) * ((soulCheck) ? 2 : 1) * ((soulSandValleyCheck) ? 2 : 1);
//            if (random.nextInt(100) + 1 < spawnChance) {
//                Registry<Block> registry = world.getRegistryManager().getOrThrow(RegistryKeys.BLOCK);
//                Optional<Block> gourd = registry.getOptionalValue(this.gourdBlock);
//                if (gourd.isPresent()) {
//                    BlockState gourdState = gourd.get().getDefaultState();
//                    if (gourdState.isOf(Blocks.MELON)) {
//                        ModEntities.MELON_CRITTER.spawn(world, pos, SpawnReason.NATURAL);
//                    } else if (gourdState.isOf(Blocks.PUMPKIN)) {
//                        ModEntities.PUMPKIN_CRITTER.spawn(world, pos, SpawnReason.NATURAL);
//                    } else {
//                        return;
//                    }
//                    world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
//                    world.playSound(null, pos, SoundEvents.ENTITY_ALLAY_AMBIENT_WITH_ITEM, SoundCategory.BLOCKS, 1F, 1F);
//                    world.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 10, 0.5, 0.5, 0.5, 0F);
//                }
//            }
//        }
//    }
}
