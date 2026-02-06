package yelf42.cropcritters.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.WorldView;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yelf42.cropcritters.CropCritters;

@Mixin(Biome.class)
public abstract class BiomeMixin {

    @Inject(method = "canSetSnow", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldView;getLightLevel(Lnet/minecraft/world/LightType;Lnet/minecraft/util/math/BlockPos;)I", shift = At.Shift.AFTER), cancellable = true)
    public void injectSnowOnCrops(WorldView world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        BlockState blockState = world.getBlockState(pos);
        if (blockState.isIn(CropCritters.SNOW_FALL_KILLS) || (blockState.getBlock() instanceof CropBlock cropBlock && !world.getBlockState(pos.up()).isOf(cropBlock))) {
            cir.setReturnValue(true);
        }
    }

}
