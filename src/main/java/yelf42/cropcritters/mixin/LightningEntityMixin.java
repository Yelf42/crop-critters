package yelf42.cropcritters.mixin;

import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LightningEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yelf42.cropcritters.CropCritters;
import yelf42.cropcritters.blocks.MazewoodSaplingBlock;
import yelf42.cropcritters.blocks.MazewoodSaplingBlockEntity;
import yelf42.cropcritters.blocks.ModBlocks;
import yelf42.cropcritters.events.WeedGrowNotifier;

import java.util.ArrayList;

import static net.minecraft.block.Block.pushEntitiesUpBeforeBlockChange;

@Mixin(LightningEntity.class)
public abstract class LightningEntityMixin {

    @Shadow private boolean cosmetic;

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LightningEntity;spawnFire(I)V", shift = At.Shift.AFTER))
    public void tick(CallbackInfo ci) {
        LightningEntity self = (LightningEntity) (Object)this;
        Vec3d pos = self.getPos();
        BlockPos blockPos = BlockPos.ofFloored(pos.x, pos.y - 1.0E-6, pos.z);
        strikeCrops(self.getWorld(), blockPos, self.getRandom());
    }

    @Unique
    private void strikeCrops(World world, BlockPos pos, Random random) {
        if (this.cosmetic) return;
        if (!(world instanceof ServerWorld)) return;

        ArrayList<BlockPos> checkLocations = new ArrayList<>();

        for(int i = 0; i < 3; ++i) {
            BlockPos rPos = pos.add(random.nextInt(3) - 1, 0, random.nextInt(3) - 1);
            if (i == 0) rPos = pos;
            BlockState struckState = world.getBlockState(rPos);
            if (struckState.isOf(Blocks.FARMLAND)) {
                checkLocations.add(rPos.up());
                pushEntitiesUpBeforeBlockChange(Blocks.FARMLAND.getDefaultState(), Blocks.DIRT.getDefaultState(), world, rPos);
                world.setBlockState(rPos, Blocks.DIRT.getDefaultState(), Block.NOTIFY_LISTENERS);
            } else if (struckState.isOf(ModBlocks.SOUL_FARMLAND)) {
                checkLocations.add(rPos.up());
                pushEntitiesUpBeforeBlockChange(ModBlocks.SOUL_FARMLAND.getDefaultState(), Blocks.SOUL_SOIL.getDefaultState(), world, rPos);
                world.setBlockState(rPos, Blocks.SOUL_SOIL.getDefaultState(), Block.NOTIFY_LISTENERS);
            }
        }

        if (checkLocations.isEmpty()) return;
        for (BlockPos posCrop : checkLocations) {
            BlockState toCheck = world.getBlockState(posCrop);
            if (toCheck.getBlock() instanceof CropBlock || toCheck.getBlock() instanceof StemBlock) {
                world.setBlockState(posCrop, Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
                if (compareBlockPosXZ(posCrop, pos) && MazewoodSaplingBlockEntity.isWall(pos)) {
                    world.setBlockState(posCrop, ModBlocks.MAZEWOOD_SAPLING.getDefaultState(), Block.NOTIFY_LISTENERS);
                    WeedGrowNotifier.notifyEvent(world, posCrop);
                }
            }
        }

    }

    @Unique
    private boolean compareBlockPosXZ(BlockPos a, BlockPos b) {
        return a.getX() == b.getX() && a.getZ() == b.getZ();
    }

}
