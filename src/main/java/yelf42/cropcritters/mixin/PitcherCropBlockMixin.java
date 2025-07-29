package yelf42.cropcritters.mixin;

import net.minecraft.block.*;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
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
    private static void cancelGrowth(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
        if (!(world.getBlockState(pos.down()).isOf(Blocks.FARMLAND)) && !(world.getBlockState(pos.down()).isOf(Blocks.FARMLAND))) ci.cancel();
    }

    // Chance to spawn critter on just matured
    @Inject(method = "tryGrow", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/PitcherCropBlock;isDoubleTallAtAge(I)Z", shift = At.Shift.BY, by = 2), cancellable = true)
    private static void removeNutrientsAndSpawnCritters(ServerWorld world, BlockState state, BlockPos pos, int amount, CallbackInfo ci) {
        if (state.get(AGE, 0) <= 3) return;

        BlockState soilCheck = world.getBlockState(pos.down());
        if (soilCheck.isOf(Blocks.FARMLAND)) {
            BlockState toDirt = (world.random.nextInt(4) == 0) ? Blocks.DIRT.getDefaultState() : (world.random.nextInt(2) == 0) ? Blocks.ROOTED_DIRT.getDefaultState() : Blocks.COARSE_DIRT.getDefaultState();
            world.setBlockState(pos.down(), toDirt, Block.NOTIFY_LISTENERS);
        } else if (soilCheck.isOf(ModBlocks.SOUL_FARMLAND)){
            BlockState toDirt = (world.random.nextInt(2) == 0) ? Blocks.SOUL_SOIL.getDefaultState() : Blocks.SOUL_SAND.getDefaultState();
            world.setBlockState(pos.down(), toDirt, Block.NOTIFY_LISTENERS);
        } else {
            return;
        }
        if (spawnCritter(world, world.random, pos)) ci.cancel();
    }

    @Unique
    private static boolean spawnCritter(ServerWorld world, Random random, BlockPos pos) {
        BlockState soil = world.getBlockState(pos.down());
        boolean bottomHalf = world.getBlockState(pos).get(HALF) == DoubleBlockHalf.LOWER;
        boolean soulCheck = soil.isOf(Blocks.SOUL_SOIL) || soil.isOf(Blocks.SOUL_SAND) || soil.isOf(ModBlocks.SOUL_FARMLAND);
        boolean soulSandValley = (world.getBiome(pos).matchesKey(BiomeKeys.SOUL_SAND_VALLEY));
        int spawnChance = ConfigManager.CONFIG.critter_spawn_chance * ((soulCheck) ? 2 : 1) * ((soulSandValley) ? 2 : 1);
        if (bottomHalf && random.nextInt(100) + 1 < spawnChance) {
            // TODO spawn pitcher critter
            world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
            // TODO Particles, SFX
            return true;
        }
        return false;
    }

}
