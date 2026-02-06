package yelf42.cropcritters.mixin;

import net.minecraft.block.FarmlandBlock;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FarmlandBlock.class)
public abstract class FarmlandBlockMixin {

    @Inject(method = "isWaterNearby", at = @At("HEAD"), cancellable = true)
    private static void isWaterNearby(WorldView world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (!world.getBiome(pos).value().hasPrecipitation()) {
            for(BlockPos blockPos : BlockPos.iterate(pos.add(-2, 0, -2), pos.add(2, 1, 2))) {
                if (world.getFluidState(blockPos).isIn(FluidTags.WATER)) {
                    cir.setReturnValue(true);
                    return;
                }
            }
            cir.setReturnValue(false);
        }
    }

}
