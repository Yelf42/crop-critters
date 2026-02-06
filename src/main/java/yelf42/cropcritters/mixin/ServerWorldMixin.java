package yelf42.cropcritters.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yelf42.cropcritters.area_affectors.AffectorPositions;
import yelf42.cropcritters.blocks.ModBlocks;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin extends World {

    public ServerWorldMixin(MutableWorldProperties properties, RegistryKey<World> registryRef, DynamicRegistryManager registryManager, RegistryEntry<DimensionType> dimensionEntry, boolean isClient, boolean debugWorld, long seed, int maxChainedNeighborUpdates) {
        super(properties, registryRef, registryManager, dimensionEntry, isClient, debugWorld, seed, maxChainedNeighborUpdates);
    }

    @Inject(method = "onBlockStateChanged", at = @At("HEAD"))
    public void injectAreaAffectorCheck(BlockPos pos, BlockState oldState, BlockState newState, CallbackInfo ci) {
        AffectorPositions.onBlockStateChange(ServerWorld.class.cast(this), pos, oldState, newState);
    }

    @Inject(method = "tickIceAndSnow", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/biome/Biome;canSetSnow(Lnet/minecraft/world/WorldView;Lnet/minecraft/util/math/BlockPos;)Z", shift = At.Shift.AFTER))
    public void injectFarmlandSnowFall(BlockPos pos, CallbackInfo ci) {
        BlockPos blockPos = this.getTopPosition(Heightmap.Type.MOTION_BLOCKING, pos).down();
        BlockState blockState = this.getBlockState(blockPos);
        if (blockState.isOf(Blocks.FARMLAND)) {
            Block.pushEntitiesUpBeforeBlockChange(blockState, Blocks.DIRT.getDefaultState(), this, blockPos);
            this.setBlockState(blockPos, Blocks.DIRT.getDefaultState());
        } else if (blockState.isOf(ModBlocks.SOUL_FARMLAND)) {
            Block.pushEntitiesUpBeforeBlockChange(blockState, Blocks.SOUL_SOIL.getDefaultState(), this, blockPos);
            this.setBlockState(blockPos, Blocks.SOUL_SOIL.getDefaultState());
        }
    }

}
