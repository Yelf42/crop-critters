package yelf42.cropcritters.world;

import net.minecraft.registry.*;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FeatureConfig;
import yelf42.cropcritters.CropCritters;

public class ModConfiguredFeatures {

    public static final Identifier DEAD_CORAL_SHELF_FEATURE_ID = Identifier.of("cropcritters", "dead_coral_shelf");
    public static final DeadCoralShelfFeature DEAD_CORAL_SHELF_FEATURE = new DeadCoralShelfFeature(DeadCoralShelfFeatureConfig.CODEC);

    public static final RegistryKey<ConfiguredFeature<?, ?>> DEAD_CORAL_SHELF_KEY = registerKey("dead_coral_shelf");

    public static void bootstrap(Registerable<ConfiguredFeature<?, ?>> context) {
        //Registry.register(Registries.FEATURE, DEAD_CORAL_SHELF_FEATURE_ID, DEAD_CORAL_SHELF_FEATURE);
        register(context, DEAD_CORAL_SHELF_KEY, new DeadCoralShelfFeature(DeadCoralShelfFeatureConfig.CODEC), new DeadCoralShelfFeatureConfig(12, Identifier.of("minecraft", "netherite_block")));
    }

    public static RegistryKey<ConfiguredFeature<?, ?>> registerKey(String name) {
        return RegistryKey.of(RegistryKeys.CONFIGURED_FEATURE, Identifier.of(CropCritters.MOD_ID, name));
    }

    private static <FC extends FeatureConfig, F extends Feature<FC>> void register(Registerable<ConfiguredFeature<?, ?>> context,
                                                                                   RegistryKey<ConfiguredFeature<?, ?>> key, F feature, FC configuration) {
        context.register(key, new ConfiguredFeature<>(feature, configuration));
    }
}
