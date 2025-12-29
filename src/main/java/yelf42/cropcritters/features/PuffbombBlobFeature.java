package yelf42.cropcritters.features;

import com.mojang.serialization.Codec;
import net.minecraft.block.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;
import yelf42.cropcritters.blocks.ModBlocks;

import java.util.ArrayList;
import java.util.List;

public class PuffbombBlobFeature extends Feature<DefaultFeatureConfig> {
    private record Sphere(Vec3d pos, double radius) {
        private boolean isWithinDistance(BlockPos blockPos) {
            return blockPos.getSquaredDistance(pos) <= radius * radius;
        }
    }


    public PuffbombBlobFeature(Codec<DefaultFeatureConfig> configCodec) {
        super(configCodec);
    }

    @Override
    public boolean generate(FeatureContext<DefaultFeatureConfig> context) {
        StructureWorldAccess world = context.getWorld();
        BlockPos centerPos = context.getOrigin();
        Random random = context.getRandom();
        ChunkGenerator chunkGenerator = context.getGenerator();

        // Seed center block
        BlockState toPlace = ModBlocks.PUFFBOMB_MUSHROOM_BLOCK.getDefaultState();
        this.setBlockState(world, centerPos, toPlace);

        // Generate spheres
        Vec3d trueCenter = centerPos.toBottomCenterPos();
        List<Sphere> spheres = new ArrayList<>();
        spheres.add(new Sphere(trueCenter, 2.0));
        for (int i = 0; i <= random.nextInt(3); i++) {
            double a = random.nextDouble() * 2.0 * Math.PI;
            double rad = 1.5 + random.nextDouble() * 2.0;
            double dist = random.nextDouble() * (3.0 / rad);
            Vec3d newCenter = new Vec3d(Math.cos(a), 0.0 - (random.nextDouble() * (rad / 2.0)), Math.sin(a)).multiply(dist).add(trueCenter);
            spheres.add(new Sphere(newCenter, rad));
        }

        // Try placing blocks and such
        Iterable<BlockPos> iterable = BlockPos.iterateOutwards(centerPos, 4,4,4);
        for(BlockPos blockPos : iterable) {
            if (blockPos.getY() < centerPos.getY()
                    || !hasNeighbours(world, blockPos)
                    || !inSphere(blockPos, spheres)
                    || !isReplaceable(world, blockPos)) continue;
            this.setBlockState(world, blockPos, toPlace);
        }
        return true;
    }

    private boolean hasNeighbours(WorldAccess world, BlockPos pos) {
        if (world.getBlockState(pos.north()).isOf(ModBlocks.PUFFBOMB_MUSHROOM_BLOCK)) return true;
        if (world.getBlockState(pos.east()).isOf(ModBlocks.PUFFBOMB_MUSHROOM_BLOCK)) return true;
        if (world.getBlockState(pos.south()).isOf(ModBlocks.PUFFBOMB_MUSHROOM_BLOCK)) return true;
        if (world.getBlockState(pos.west()).isOf(ModBlocks.PUFFBOMB_MUSHROOM_BLOCK)) return true;
        if (world.getBlockState(pos.up()).isOf(ModBlocks.PUFFBOMB_MUSHROOM_BLOCK)) return true;
        return (world.getBlockState(pos.down()).isOf(ModBlocks.PUFFBOMB_MUSHROOM_BLOCK));
    }

    private boolean inSphere(BlockPos pos, List<Sphere> spheres) {
        for (Sphere sphere : spheres) {
            if (sphere.isWithinDistance(pos)) return true;
        }
        return false;
    }

    private static boolean isReplaceable(StructureWorldAccess world, BlockPos pos) {
        if (world.testBlockState(pos, AbstractBlock.AbstractBlockState::isReplaceable)) return true;
        return (world.getBlockState(pos).getBlock() instanceof PlantBlock);
    }
}