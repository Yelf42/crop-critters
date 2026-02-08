package yelf42.cropcritters.mixin;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.DecoratedPotBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yelf42.cropcritters.blocks.ModBlocks;
import yelf42.cropcritters.blocks.SoulPotBlockEntity;
import yelf42.cropcritters.items.ModItems;

@Mixin(FlowerPotBlock.class)
public abstract class FlowerPotBlockMixin {

    @Inject(method = "hasRandomTicks", at = @At("HEAD"), cancellable = true)
    private void overrideRandomTicks(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (state.isOf(Blocks.POTTED_WITHER_ROSE)) cir.setReturnValue(true);
    }

    @Inject(method = "randomTick", at = @At(value = "HEAD"), cancellable = true)
    private static void injectWitherToSoulRose(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
        if (state.isOf(Blocks.POTTED_WITHER_ROSE)) {
            BlockState below = world.getBlockState(pos.down());
            if (below.hasBlockEntity()) {
                BlockEntity blockEntity = world.getBlockEntity(pos.down());
                if (blockEntity instanceof SoulPotBlockEntity soulPotBlockEntity) {
                    ItemStack inv = soulPotBlockEntity.getStack();
                    if (inv != null && inv.isOf(ModItems.LOST_SOUL) && inv.getCount() >= 24) {
                        soulPotBlockEntity.setStack(inv.copyWithCount(inv.getCount() - 24));
                        world.setBlockState(pos, ModBlocks.POTTED_SOUL_ROSE.getDefaultState());
                        ci.cancel();
                    }
                }
            }
        }
    }
}
