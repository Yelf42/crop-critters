package yelf42.cropcritters.mixin;

import net.minecraft.block.*;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.SpawnReason;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.biome.BiomeKeys;
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

import static net.minecraft.block.Block.pushEntitiesUpBeforeBlockChange;

@Mixin(PitcherCropBlock.class)
public abstract class PitcherCropBlockMixin {

    @Shadow @Final public static IntProperty AGE;
    @Shadow @Final public static EnumProperty<DoubleBlockHalf> HALF;

    @Inject(method = "canPlantOnTop", at = @At("HEAD"), cancellable = true)
    private void allowPlantOnSoulAndDirt(BlockState floor, BlockView world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (floor.isIn(BlockTags.DIRT)) cir.setReturnValue(true);
    }

    // Stop growth on non-farmland
    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    private void cancelGrowth(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
        if (!(world.getBlockState(pos.down()).isOf(Blocks.FARMLAND) || world.getBlockState(pos.down()).isOf(ModBlocks.SOUL_FARMLAND))) ci.cancel();
    }

    @Inject(method = "randomTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/PitcherCropBlock;tryGrow(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;I)V", shift = At.Shift.AFTER), cancellable = true)
    private void removeNutrientsAndSpawnCritters(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
        state = world.getBlockState(pos);
        if (state.get(AGE, 0) <= 3) return;

        BlockState soilCheck = world.getBlockState(pos.down());
        if (soilCheck.isOf(Blocks.FARMLAND)) {
            pushEntitiesUpBeforeBlockChange(Blocks.FARMLAND.getDefaultState(), Blocks.DIRT.getDefaultState(), world, pos.down());
            BlockState toDirt = (world.random.nextInt(4) == 0) ? Blocks.DIRT.getDefaultState() : (world.random.nextInt(2) == 0) ? Blocks.ROOTED_DIRT.getDefaultState() : Blocks.COARSE_DIRT.getDefaultState();
            world.setBlockState(pos.down(), toDirt, Block.NOTIFY_LISTENERS);
        } else if (soilCheck.isOf(ModBlocks.SOUL_FARMLAND)){
            pushEntitiesUpBeforeBlockChange(ModBlocks.SOUL_FARMLAND.getDefaultState(), Blocks.SOUL_SOIL.getDefaultState(), world, pos.down());
            BlockState toDirt = (world.random.nextInt(2) == 0) ? Blocks.SOUL_SOIL.getDefaultState() : Blocks.SOUL_SAND.getDefaultState();
            world.setBlockState(pos.down(), toDirt, Block.NOTIFY_LISTENERS);
        } else {
            return;
        }

        if (spawnCritter(world, world.random, pos)) return;
    }

    @Unique
    private static boolean spawnCritter(ServerWorld world, Random random, BlockPos pos) {
        BlockState soil = world.getBlockState(pos.down());
        boolean bottomHalf = world.getBlockState(pos).get(HALF) == DoubleBlockHalf.LOWER;
        boolean soulCheck = soil.isOf(Blocks.SOUL_SOIL) || soil.isOf(Blocks.SOUL_SAND) || soil.isOf(ModBlocks.SOUL_FARMLAND);
        boolean soulSandValley = (world.getBiome(pos).matchesKey(BiomeKeys.SOUL_SAND_VALLEY));
        int spawnChance = ConfigManager.CONFIG.critterSpawnChance * ((soulCheck) ? 2 : 1) * ((soulSandValley) ? 2 : 1);
        if (bottomHalf && random.nextInt(100) + 1 < spawnChance) {
            ModEntities.PITCHER_CRITTER.spawn(world, pos, SpawnReason.NATURAL);
            world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
            world.playSound(null, pos, SoundEvents.ENTITY_ALLAY_AMBIENT_WITH_ITEM, SoundCategory.BLOCKS, 1F, 1F);
            world.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 10, 0.5, 0.5, 0.5, 0F);
            return true;
        }
        return false;
    }

}
