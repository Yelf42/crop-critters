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
}
