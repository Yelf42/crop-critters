package yelf42.cropcritters.config;

import net.minecraft.block.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.event.GameEvent;
import yelf42.cropcritters.CropCritters;
import yelf42.cropcritters.blocks.ModBlocks;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.block.Block.pushEntitiesUpBeforeBlockChange;

public class WeedPlacement {

    public static boolean canWeedsReplace(BlockState state) {
        if (state.contains(Properties.DOUBLE_BLOCK_HALF)) return false;
        return state.isOf(Blocks.AIR)
                || (state.getBlock() instanceof PlantBlock && !state.isIn(CropCritters.WEEDS) && !state.isIn(CropCritters.IMMUNE_PLANTS));
    }

    // List of weeds not in the weighted lists:
    // Withering Spiteweed (only generates in soul sand valley)
    // Liverwort (only generates when raining)
    // Mazewood sapling (generates from lightning, not this)

    private static final List<Pair<BlockState, Double>> WEIGHTED_NETHER_WEEDS = new ArrayList<>(List.of(
            new Pair<>(ModBlocks.WITHERING_SPITEWEED.getDefaultState(), 0.2),
            new Pair<>(ModBlocks.WAFTGRASS.getDefaultState(), 0.25),
            new Pair<>(ModBlocks.BONE_TRAP.getDefaultState(), 0.25),
            new Pair<>(ModBlocks.CRIMSON_THORNWEED.getDefaultState(), 0.3)
    ));

    private static final List<Pair<BlockState, Double>> WEIGHTED_OVERWORLD_WEEDS = new ArrayList<>(List.of(
            new Pair<>(ModBlocks.POPPER_PLANT.getDefaultState(), 0.15),
            new Pair<>(ModBlocks.PUFFBOMB_MUSHROOM.getDefaultState(), 0.15),
            new Pair<>(ModBlocks.STRANGLE_FERN.getDefaultState(), 0.3),
            new Pair<>(ModBlocks.CRAWL_THISTLE.getDefaultState(), 0.4)
    ));

    // Assumes weights add up to 1
    public static <A> A getFromWeightedList(List<Pair<A, Double>> list) {
        double r = Math.random();
        for (Pair<A, Double> pair : list) {
            r -= pair.getRight();
            if (r <= 0) return pair.getLeft();
        }
        return list.getLast().getLeft();
    }

    public static void generateWeed(BlockState state, ServerWorld world, BlockPos pos, Random random, boolean nether) {
        // Count how many neighbours are the same type of crop
        // More identical crops increases chance of weed growth
        float monoCount = 1F;
        if (ConfigManager.CONFIG.monoculturePenalize) {
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    if (i == j && j == 0) continue;
                    BlockState cropToCheck = world.getBlockState(pos.add(i,0, j));
                    monoCount += cropToCheck.isOf(state.getBlock()) ? 1F : 0F;
                }
            }
            // Quadratic penalty increase for monocultural practices
            monoCount = (monoCount * monoCount) / 8F;
        }
        boolean growOverworldWeed = random.nextInt(100) + 1 < (float)ConfigManager.CONFIG.regularWeedChance + (monoCount);
        boolean growNetherWeed = random.nextInt(100) + 1 < (float)ConfigManager.CONFIG.netherWeedChance + (monoCount);

        // Break early, no weeds can grow
        if (!growOverworldWeed && !growNetherWeed) return;

        if (nether && growNetherWeed) {
            // Special case checks
            if (world.getBiome(pos).matchesKey(BiomeKeys.SOUL_SAND_VALLEY)) {
                placeUniqueWeed(ModBlocks.WITHERING_SPITEWEED.getDefaultState(), world, pos, Blocks.BLACKSTONE.getDefaultState());
                return;
            }

            // Default
            placeNetherWeed(getFromWeightedList(WEIGHTED_NETHER_WEEDS), world, pos, random);
        } else if (!nether && growOverworldWeed) {
            // Special case checks
            if (world.hasRain(pos)) {
                placeOverworldWeed(ModBlocks.LIVERWORT.getDefaultState().with(MultifaceBlock.getProperty(Direction.DOWN), true), world, pos, random);
                return;
            }

            // Default
            placeOverworldWeed(getFromWeightedList(WEIGHTED_OVERWORLD_WEEDS), world, pos, random);
        }
    }

    private static void placeNetherWeed(BlockState weedState, ServerWorld world, BlockPos pos, Random random) {
        pushEntitiesUpBeforeBlockChange(ModBlocks.SOUL_FARMLAND.getDefaultState(), Blocks.SOUL_SOIL.getDefaultState(), world, pos.down());
        world.setBlockState(pos.down(), (random.nextInt(2) == 0) ? Blocks.SOUL_SOIL.getDefaultState() : Blocks.SOUL_SAND.getDefaultState(), Block.NOTIFY_LISTENERS);
        world.setBlockState(pos, weedState);
        world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(null, weedState));
    }

    private static void placeOverworldWeed(BlockState weedState, ServerWorld world, BlockPos pos, Random random) {
        pushEntitiesUpBeforeBlockChange(Blocks.FARMLAND.getDefaultState(), Blocks.COARSE_DIRT.getDefaultState(), world, pos.down());
        world.setBlockState(pos.down(), (random.nextInt(2) == 0) ? Blocks.COARSE_DIRT.getDefaultState() : Blocks.ROOTED_DIRT.getDefaultState(), Block.NOTIFY_LISTENERS);
        world.setBlockState(pos, weedState);
        world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(null, weedState));
    }

    private static void placeUniqueWeed(BlockState weedState, ServerWorld world, BlockPos pos, BlockState below) {
        pushEntitiesUpBeforeBlockChange(ModBlocks.SOUL_FARMLAND.getDefaultState(), below, world, pos.down());
        world.setBlockState(pos.down(), below, Block.NOTIFY_LISTENERS);
        world.setBlockState(pos, weedState);
        world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(null, weedState));
    }
}
