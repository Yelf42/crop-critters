package yelf42.cropcritters.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.dispenser.ShearsDispenserBehavior;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.event.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yelf42.cropcritters.blocks.ModBlocks;
import yelf42.cropcritters.blocks.PopperPlantBlock;
import yelf42.cropcritters.items.ModItems;

@Mixin(ShearsDispenserBehavior.class)
public class ShearsDispenserBehaviourMixin {

    @Inject(method = "tryShearBlock", at = @At("TAIL"), cancellable = true)
    private static void shearPopperPlant(ServerWorld world, ItemStack tool, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        BlockState blockState = world.getBlockState(pos);
        if (blockState.isOf(ModBlocks.POPPER_PLANT) && (blockState.get(PopperPlantBlock.AGE, 0) == PopperPlantBlock.MAX_AGE)) {
            world.emitGameEvent((Entity)null, GameEvent.SHEAR, pos);
            world.playSound(null, pos, SoundEvents.ITEM_SHEARS_SNIP, SoundCategory.PLAYERS, 1.0F, 1.0F);

            Vec3d center = pos.toCenterPos();
            ItemStack itemStack = new ItemStack(ModItems.POPPER_POD);
            ItemEntity itemEntity = new ItemEntity(world, center.x, center.y, center.z, itemStack);
            itemEntity.setToDefaultPickupDelay();
            world.spawnEntity(itemEntity);

            world.setBlockState(pos, blockState.with(PopperPlantBlock.AGE, 0));

            cir.setReturnValue(true);
        }
    }

}
