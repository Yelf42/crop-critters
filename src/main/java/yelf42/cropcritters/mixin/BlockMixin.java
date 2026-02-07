package yelf42.cropcritters.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yelf42.cropcritters.config.AffectorsHelper;

import java.util.List;

@Mixin(Block.class)
public class BlockMixin {

    // Iron soul rose chance to gain more crop yield
    @Inject(method = "getDroppedStacks(Lnet/minecraft/block/BlockState;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/entity/BlockEntity;)Ljava/util/List;", at = @At("RETURN"), cancellable = true)
    private static void onGetDroppedStacks4Param(BlockState state, ServerWorld world, BlockPos pos, @Nullable BlockEntity blockEntity, CallbackInfoReturnable<List<ItemStack>> cir) {
        if (state.getBlock() instanceof CropBlock cropBlock && !cropBlock.isFertilizable(world, pos, state)) {
            if (world.random.nextDouble() < 0.15F * AffectorsHelper.ironSoulRoseCheck(world, pos)) {
                List<ItemStack> list = cir.getReturnValue();
                for (int i = list.size() - 1; i >= 0; i--) {
                    list.add(i + 1, list.get(i));
                }
                world.spawnParticles(ParticleTypes.GLOW, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 5, 0.5, 0.5, 0.5, 0.0);
                cir.setReturnValue(list);
            }
        }
    }
    @Inject(method = "getDroppedStacks(Lnet/minecraft/block/BlockState;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/entity/BlockEntity;Lnet/minecraft/entity/Entity;Lnet/minecraft/item/ItemStack;)Ljava/util/List;", at = @At("RETURN"), cancellable = true)
    private static void onGetDroppedStacks6Param(BlockState state, ServerWorld world, BlockPos pos, @Nullable BlockEntity blockEntity, @Nullable Entity entity, ItemStack stack, CallbackInfoReturnable<List<ItemStack>> cir) {
        if (state.getBlock() instanceof CropBlock cropBlock && !cropBlock.isFertilizable(world, pos, state)) {
            if (world.random.nextDouble() < 0.15F * AffectorsHelper.ironSoulRoseCheck(world, pos)) {
                List<ItemStack> list = cir.getReturnValue();
                for (int i = list.size() - 1; i >= 0; i--) {
                    list.add(i + 1, list.get(i));
                }
                world.spawnParticles(ParticleTypes.GLOW, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 5, 0.5, 0.5, 0.5, 0.0);
                cir.setReturnValue(list);
            }
        }
    }
}
