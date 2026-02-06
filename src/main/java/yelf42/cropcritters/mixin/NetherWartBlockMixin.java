package yelf42.cropcritters.mixin;

import net.minecraft.block.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yelf42.cropcritters.config.AffectorsHelper;
import yelf42.cropcritters.blocks.ModBlocks;
import yelf42.cropcritters.config.ConfigManager;
import yelf42.cropcritters.config.CritterHelper;
import yelf42.cropcritters.config.WeedHelper;

@Mixin(NetherWartBlock.class)
public abstract class NetherWartBlockMixin {
    @Shadow @Final public static IntProperty AGE;

    @Inject(method = "canPlantOnTop", at = @At("HEAD"), cancellable = true)
    private void allowPlantOnSoulStuff(BlockState floor, BlockView world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (floor.isOf(Blocks.SOUL_SAND) || floor.isOf(ModBlocks.SOUL_FARMLAND) || floor.isOf(Blocks.SOUL_SOIL)) {
            cir.setReturnValue(true);
            return;
        }
        cir.setReturnValue(false);
    }

    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    private static void injectIntoRandomTicksHead(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
        if (random.nextInt(100) < ConfigManager.CONFIG.goldSoulRoseSlowdown && AffectorsHelper.copperSoulRoseCheck(world, pos)) ci.cancel();
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

        if (CritterHelper.spawnCritter(world, state, random, pos)) return;

        WeedHelper.generateWeed(state, world, pos, random, true);
    }
}
