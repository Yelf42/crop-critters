package yelf42.cropcritters.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.HoeItem;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yelf42.cropcritters.events.ModEventHandlers;

@Mixin(HoeItem.class)
public abstract class HoeItemMixin {

    @Inject(method = "useOnBlock", at = @At("HEAD"), cancellable = true)
    private void onHoeUseOnBlock(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        World world = context.getWorld();
        PlayerEntity player = context.getPlayer();
        if (player == null) return;

        BlockPos pos = context.getBlockPos();
        BlockState state = world.getBlockState(pos);

        if (ModEventHandlers.handleHoeUse(world, pos, state)) {
            context.getStack().damage(1, player, context.getHand().getEquipmentSlot());
            cir.setReturnValue(ActionResult.SUCCESS);
        }
    }
}
