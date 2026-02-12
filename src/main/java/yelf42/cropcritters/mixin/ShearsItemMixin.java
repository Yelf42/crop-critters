package yelf42.cropcritters.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.ShearsItem;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yelf42.cropcritters.events.ModEventHandlers;

@Mixin(ShearsItem.class)
public abstract class ShearsItemMixin {

    @Inject(method = "useOnBlock", at = @At("HEAD"), cancellable = true)
    private void onShearsUseOnBlock(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        World world = context.getWorld();

        PlayerEntity player = context.getPlayer();
        if (player == null) return;

        ItemStack stack = player.getStackInHand(context.getHand());
        BlockPos pos = context.getBlockPos();
        BlockState state = world.getBlockState(pos);

        if (ModEventHandlers.handleShearsUse(player, world, stack, pos, state)) {
            cir.setReturnValue(ActionResult.SUCCESS);
        }
    }

}
