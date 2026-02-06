package yelf42.cropcritters.config;

import com.mojang.serialization.Codec;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;
import net.minecraft.world.World;
import yelf42.cropcritters.CropCritters;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

// TODO actually test with a farming mod
public class RecognizedCropsState extends PersistentState {
    private final Set<Item> knownCrops = new HashSet<>();

    private static final Codec<RecognizedCropsState> CODEC = Codec.list(Registries.ITEM.getCodec())
            .xmap(
                    list -> {
                        RecognizedCropsState state = new RecognizedCropsState();
                        state.knownCrops.addAll(list);
                        return state;
                    },
                    state -> new ArrayList<>(state.knownCrops)
            );

    private static final PersistentStateType<RecognizedCropsState> type = new PersistentStateType<>(
            CropCritters.MOD_ID,
            RecognizedCropsState::new,
            CODEC,
            null
    );

    private RecognizedCropsState() {
        // Default constructor for empty state
    }

    public void addCrop(Item item) {
        if (knownCrops.add(item)) {
            this.markDirty(); // mark for save
        }
    }

    public boolean hasCrop(Item item) {
        return knownCrops.contains(item);
    }

    public static RecognizedCropsState getServerState(MinecraftServer server) {
        ServerWorld serverWorld = server.getWorld(World.OVERWORLD);
        assert serverWorld != null;
        RecognizedCropsState state = serverWorld.getPersistentStateManager().getOrCreate(type);
        state.markDirty();
        return state;
    }

}
