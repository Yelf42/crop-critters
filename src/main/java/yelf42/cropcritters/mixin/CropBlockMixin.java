package yelf42.cropcritters.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.event.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yelf42.cropcritters.blocks.ModBlocks;
import yelf42.cropcritters.config.ConfigManager;

@Mixin(CropBlock.class)
public abstract class CropBlockMixin {

    // Allows crops to be planted on SOUL_FARMLAND
    @Inject(method = "canPlantOnTop", at = @At("HEAD"), cancellable = true)
    private void allowPlantOnSoulFarmland(BlockState floor, BlockView world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (floor.isOf(ModBlocks.SOUL_FARMLAND)) {
            cir.setReturnValue(true);
        }
    }

    // If SOUL_FARMLAND, ignore vanilla moisture stuff and just return 8.f
    @Inject(method = "getAvailableMoisture", at = @At("HEAD"), cancellable = true)
    private static void soulBasedMoisture(Block block, BlockView world, BlockPos pos, CallbackInfoReturnable<Float> cir) {
        BlockState blockState = world.getBlockState(pos.down());
        if (blockState.isOf(ModBlocks.SOUL_FARMLAND)) {
            cir.setReturnValue(8.f);
        }
    }

    // Inject into randomTicks to turn into critter if just turned mature

    // Inject into hasRandomTicks to make always true
    @Inject(method = "hasRandomTicks", at = @At("HEAD"), cancellable = true)
    private void overrideMatureStoppingRandomTicks(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }

    // Inject into randomTicks to turn into weed if mature
    @Inject(method = "randomTick", at = @At("TAIL"), cancellable = true)
    private static void injectWeedsIntoRandomTick(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
        if (state.getBlock() instanceof CropBlock cropBlock) {
            if (!cropBlock.isMature(state)) return;
        }

        // Count how many neighbours are the same type of crop
        // More identical crops increases chance of weed growth
        float monoCount = 1F;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == j && j == 0) continue;
                BlockState cropToCheck = world.getBlockState(pos.add(i,0, j));
                monoCount += cropToCheck.isOf(state.getBlock()) ? 1F : 0F;
            }
        }
        // Quadratic penalty increase for monocultural practices
        monoCount = (monoCount * monoCount) / (float)ConfigManager.CONFIG.monoculture_dampener;
        boolean growThistle = random.nextInt(100) + 1 < (float)ConfigManager.CONFIG.thistle_chance * monoCount;
        boolean growThornweed = random.nextInt(100) + 1 < (float)ConfigManager.CONFIG.thornweed_chance * monoCount;

        BlockState soilCheck = world.getBlockState(pos.down());
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
