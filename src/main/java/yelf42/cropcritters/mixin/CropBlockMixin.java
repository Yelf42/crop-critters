package yelf42.cropcritters.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.entity.SpawnReason;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.biome.BiomeKeys;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yelf42.cropcritters.blocks.ModBlocks;
import yelf42.cropcritters.config.ConfigManager;
import yelf42.cropcritters.config.WeedPlacement;
import yelf42.cropcritters.entity.ModEntities;

import static net.minecraft.block.Block.pushEntitiesUpBeforeBlockChange;

@Mixin(CropBlock.class)
public abstract class CropBlockMixin {

    // Allows plants to be planted on SOUL_FARMLAND
    @Inject(method = "canPlantOnTop", at = @At("HEAD"), cancellable = true)
    private void allowPlantOnSoulAndDirt(BlockState floor, BlockView world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (floor.isIn(BlockTags.DIRT)) cir.setReturnValue(true);
    }

    // If SOUL_FARMLAND, ignore vanilla moisture stuff and just return 18.f
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
    // Chance to spawn critter if just matured
    @Inject(method = "randomTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z", shift = At.Shift.AFTER))
    private static void removeNutrientsAndSpawnCritters(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
        if (state.getBlock() instanceof CropBlock cropBlock) {
            if (cropBlock.getAge(state) + 1 != cropBlock.getMaxAge()) return;
            BlockState soilCheck = world.getBlockState(pos.down());
            if (soilCheck.isOf(Blocks.FARMLAND)) {
                // Farmland to dirt
                pushEntitiesUpBeforeBlockChange(Blocks.FARMLAND.getDefaultState(), Blocks.DIRT.getDefaultState(), world, pos.down());
                BlockState toDirt = (random.nextInt(4) == 0) ? Blocks.DIRT.getDefaultState() : (random.nextInt(2) == 0) ? Blocks.ROOTED_DIRT.getDefaultState() : Blocks.COARSE_DIRT.getDefaultState();
                world.setBlockState(pos.down(), toDirt, Block.NOTIFY_LISTENERS);
            } else if (soilCheck.isOf(ModBlocks.SOUL_FARMLAND)){
                // Soul farmland to soul blocks
                pushEntitiesUpBeforeBlockChange(Blocks.FARMLAND.getDefaultState(), Blocks.SOUL_SOIL.getDefaultState(), world, pos.down());
                BlockState toDirt = (random.nextInt(2) == 0) ? Blocks.SOUL_SOIL.getDefaultState() : Blocks.SOUL_SAND.getDefaultState();
                world.setBlockState(pos.down(), toDirt, Block.NOTIFY_LISTENERS);
            } else {
                return;
            }

            if (spawnCritter(world, state, random, pos)) return;
        }
    }

    // Try spawn critter if soul_sand_valley
    // Try to generate weed if on farmland (scale chance with age)
    // Stop ticking / aging if not on farmland
    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    private static void stopGrowthOnDirtAndSpawnSoulSandValleyCritters(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
        BlockState soilCheck = world.getBlockState(pos.down());
        if (!(soilCheck.isOf(Blocks.FARMLAND) || soilCheck.isOf(ModBlocks.SOUL_FARMLAND))) {
            ci.cancel();
            return;
        }

        if (state.getBlock() instanceof CropBlock cropBlock) {
            if (world.getBiome(pos).matchesKey(BiomeKeys.SOUL_SAND_VALLEY)  && cropBlock.isMature(state)) {
                if (spawnCritter(world, state, random, pos)) return;
            }
            if (random.nextDouble() < 0.03 * ((double) cropBlock.getAge(state) / (cropBlock.getMaxAge() - 1))) {
                WeedPlacement.generateWeed(state, world, pos, random, soilCheck.isOf(ModBlocks.SOUL_FARMLAND));
            }
        }
    }

    @Unique
    private static boolean spawnCritter(ServerWorld world, BlockState state, Random random, BlockPos pos) {
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
            } else if (state.isOf(Blocks.BEETROOTS)) {
                ModEntities.BEETROOT_CRITTER.spawn(world, pos, SpawnReason.NATURAL);
            } else if (state.isOf(Blocks.TORCHFLOWER_CROP)) {
                ModEntities.TORCHFLOWER_CRITTER.spawn(world, pos, SpawnReason.NATURAL);
            } else {
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
