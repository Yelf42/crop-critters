package yelf42.cropcritters.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yelf42.cropcritters.blocks.ModBlocks;
import yelf42.cropcritters.events.ModEventHandlers;

@Mixin(BlockItem.class)
public abstract class BlockItemMixin {

    @Inject(method = "useOnBlock", at = @At("HEAD"), cancellable = true)
    private void onStrangleFernSporesUseOnBlock(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        World world = context.getWorld();
        if (world.isClient()) return;

        PlayerEntity player = context.getPlayer();
        if (player == null) return;

        ItemStack stack = player.getStackInHand(context.getHand());
        BlockPos pos = context.getBlockPos();
        BlockState state = world.getBlockState(pos);

        if (stack.isOf(ModBlocks.STRANGLE_FERN.asItem()) && ModEventHandlers.handleStrangleFernPlanting(player, world, stack, pos, state)) {
            cir.setReturnValue(ActionResult.SUCCESS);
        }
    }
}
