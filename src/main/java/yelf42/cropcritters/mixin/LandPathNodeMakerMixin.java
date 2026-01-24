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

    @Inject(method="getCommonNodeType", at=@At("HEAD"), cancellable = true)
    private static void injectWeedPenalties(BlockView world, BlockPos pos, CallbackInfoReturnable<PathNodeType> cir) {
        BlockState state = world.getBlockState(pos);
        // TODO Breaks wheat critter, removing for now
        // If necessary, enum addition in PathNodeType.class, then in AbstractCropCritterEntity call this.setPathfindingPenalty() in constructor;
        //if (state.isIn(CropCritters.WEEDS)) cir.setReturnValue(PathNodeType.DAMAGE_OTHER);
    }

}
