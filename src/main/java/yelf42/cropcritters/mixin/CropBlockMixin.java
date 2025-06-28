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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yelf42.cropcritters.blocks.ModBlocks;

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

        BlockState soilCheck = world.getBlockState(pos.down());
        if (soilCheck.isOf(Blocks.FARMLAND) && random.nextInt(100) < 2) {
            BlockState weedState = ModBlocks.CRAWL_THISTLE.getDefaultState();
            world.setBlockState(pos, weedState);
            world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(null, weedState));
            return;
        }

        if (soilCheck.isOf(ModBlocks.SOUL_FARMLAND) && random.nextInt(100) < 2) {
            BlockState weedState = ModBlocks.CRIMSON_THORNWEED.getDefaultState();
            world.setBlockState(pos, weedState);
            world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(null, weedState));
            return;
        }
    }
}
