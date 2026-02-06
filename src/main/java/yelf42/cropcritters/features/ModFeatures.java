package yelf42.cropcritters.features;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.CountConfig;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import yelf42.cropcritters.CropCritters;

public class ModFeatures {
    public static void initialize() {
        CropCritters.LOGGER.info("Initializing features for " + CropCritters.MOD_ID);
    }

    public static final Feature<DefaultFeatureConfig> PUFFBOMB_BLOB_FEATURE =
            Registry.register(
                    Registries.FEATURE,
                    Identifier.of(CropCritters.MOD_ID, "puffbomb_blob"),
                    new PuffbombBlobFeature(DefaultFeatureConfig.CODEC)
            );
    public static final RegistryKey<ConfiguredFeature<?, ?>> PUFFBOMB_BLOB_CONFIGURED_FEATURE =
            RegistryKey.of(
                    RegistryKeys.CONFIGURED_FEATURE,
                    Identifier.of(CropCritters.MOD_ID, "puffbomb_blob")
            );

    public static final Feature<CountConfig> LIVERWORT_FEATURE =
            Registry.register(
                    Registries.FEATURE,
                    Identifier.of(CropCritters.MOD_ID, "liverwort_patch"),
                    new LiverwortFeature(CountConfig.CODEC)
            );
    public static final RegistryKey<ConfiguredFeature<?, ?>> LIVERWORT_CONFIGURED_FEATURE =
            RegistryKey.of(
                    RegistryKeys.CONFIGURED_FEATURE,
                    Identifier.of(CropCritters.MOD_ID, "liverwort_patch")
            );

    public static final Feature<DefaultFeatureConfig> SOUL_ROSE_HINT_FEATURE =
            Registry.register(
                    Registries.FEATURE,
                    Identifier.of(CropCritters.MOD_ID, "soul_rose_hint"),
                    new SoulRoseHintFeature(DefaultFeatureConfig.CODEC)
            );
    public static final RegistryKey<ConfiguredFeature<?, ?>> SOUL_ROSE_HINT_CONFIGURED_FEATURE =
            RegistryKey.of(
                    RegistryKeys.CONFIGURED_FEATURE,
                    Identifier.of(CropCritters.MOD_ID, "soul_rose_hint")
            );
}
