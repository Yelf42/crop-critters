package yelf42.cropcritters.events;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import yelf42.cropcritters.CropCritters;

import java.util.*;

public class WeedGrowNotifier {
    private static final Map<World, Set<BlockPos>> weedsToRing = new WeakHashMap<>();
    private static final int RADIUS = 128;

    public static boolean checkWeedsToRing(World world, BlockPos bellPos) {
        Set<BlockPos> positions = weedsToRing.get(world);
        if (positions == null) return false;
        for (BlockPos weedPos : positions) {
            if (weedPos.isWithinDistance(bellPos, RADIUS)) {
                positions.remove(weedPos);
                if (positions.isEmpty()) {
                    weedsToRing.remove(world);
                }
                return true;
            }
        }
        return false;
    }

    public static void notifyEvent(World world, BlockPos eventPos) {
        weedsToRing.computeIfAbsent(world, w -> new HashSet<>())
                .add(eventPos.toImmutable());
    }

    public static void notifyRemoval(World world, BlockPos eventPos) {
        Set<BlockPos> positions = weedsToRing.get(world);
        if (positions == null) return;
        positions.remove(eventPos);
        if (positions.isEmpty()) {
            weedsToRing.remove(world);
        }
    }
}
