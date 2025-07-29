package yelf42.cropcritters.events;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import yelf42.cropcritters.CropCritters;

import java.util.*;

public class WeedGrowNotifier {
    private static final Map<World, Map<BlockPos, Long>> weedsToRing = new WeakHashMap<>();
    private static final int RADIUS = 128;
    private static final long MAX_AGE_TICKS = 20 * 60;

    public static boolean checkWeedsToRing(World world, BlockPos bellPos) {
        Map<BlockPos, Long> positions = weedsToRing.get(world);
        if (positions == null) return false;

        long currentTime = world.getTime();
        for (var entry : positions.entrySet()) {
            // Position too old, delete
            if (currentTime - entry.getValue() > MAX_AGE_TICKS) {
                positions.remove(entry.getKey());
                continue;
            }

            // Position close, delete and ring
            if (entry.getKey().isWithinDistance(bellPos, RADIUS)) {
                positions.remove(entry.getKey());
                if (positions.isEmpty()) {
                    weedsToRing.remove(world);
                }
                return true;
            }
        }
        if (positions.isEmpty()) {
            weedsToRing.remove(world);
        }

        return false;
    }

    public static void notifyEvent(World world, BlockPos eventPos) {
        weedsToRing.computeIfAbsent(world, w -> new HashMap<>());
        if (weedsToRing.get(world).size() >= 256) {
            weedsToRing.get(world).clear();
        }
        weedsToRing.get(world).put(eventPos.toImmutable(), world.getTime());
    }

    public static void notifyRemoval(World world, BlockPos eventPos) {
        Map<BlockPos, Long> positions = weedsToRing.get(world);
        if (positions == null) return;
        positions.remove(eventPos);
        if (positions.isEmpty()) {
            weedsToRing.remove(world);
        }
    }
}
