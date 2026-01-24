package yelf42.cropcritters.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.pathing.LandPathNodeMaker;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yelf42.cropcritters.CropCritters;

@Mixin(LandPathNodeMaker.class)
public abstract class LandPathNodeMakerMixin {

    // PathNodeType.DAMAGE_OTHER is only used for CACTUS and SWEET_BERRY_BUSH, which is close enough to WEEDS
    // In the future, consider custom PathNodeType just for WEEDS
    @Inject(method="getCommonNodeType", at=@At("HEAD"), cancellable = true)
    private static void injectWeedPenalties(BlockView world, BlockPos pos, CallbackInfoReturnable<PathNodeType> cir) {
        BlockState state = world.getBlockState(pos);
        if (state.isIn(CropCritters.PATH_PENALTY_WEEDS)) cir.setReturnValue(PathNodeType.DAMAGE_OTHER);
    }

}
