package yelf42.cropcritters.world.gen;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.world.gen.GenerationStep;
import yelf42.cropcritters.world.ModPlacedFeatures;

public class ModWorldGeneration {
    public static void generateModWorldGen() {
        BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), GenerationStep.Feature.RAW_GENERATION,
                ModPlacedFeatures.DEAD_CORAL_SHELF_PLACED_KEY);
    }
}
