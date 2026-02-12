package yelf42.cropcritters.features;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.feature.configurations.CountConfiguration;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.Feature;
import yelf42.cropcritters.CropCritters;

public class ModFeatures {
    public static void initialize() {
        CropCritters.LOGGER.info("Initializing features for " + CropCritters.MOD_ID);
    }

    public static final Feature<NoneFeatureConfiguration> PUFFBOMB_BLOB_FEATURE =
            Registry.register(
                    BuiltInRegistries.FEATURE,
                    Identifier.fromNamespaceAndPath(CropCritters.MOD_ID, "puffbomb_blob"),
                    new PuffbombBlobFeature(NoneFeatureConfiguration.CODEC)
            );
    public static final ResourceKey<ConfiguredFeature<?, ?>> PUFFBOMB_BLOB_CONFIGURED_FEATURE =
            ResourceKey.create(
                    Registries.CONFIGURED_FEATURE,
                    Identifier.fromNamespaceAndPath(CropCritters.MOD_ID, "puffbomb_blob")
            );

    public static final Feature<CountConfiguration> LIVERWORT_FEATURE =
            Registry.register(
                    BuiltInRegistries.FEATURE,
                    Identifier.fromNamespaceAndPath(CropCritters.MOD_ID, "liverwort_patch"),
                    new LiverwortFeature(CountConfiguration.CODEC)
            );
    public static final ResourceKey<ConfiguredFeature<?, ?>> LIVERWORT_CONFIGURED_FEATURE =
            ResourceKey.create(
                    Registries.CONFIGURED_FEATURE,
                    Identifier.fromNamespaceAndPath(CropCritters.MOD_ID, "liverwort_patch")
            );

    public static final Feature<NoneFeatureConfiguration> SOUL_ROSE_HINT_FEATURE =
            Registry.register(
                    BuiltInRegistries.FEATURE,
                    Identifier.fromNamespaceAndPath(CropCritters.MOD_ID, "soul_rose_hint"),
                    new SoulRoseHintFeature(NoneFeatureConfiguration.CODEC)
            );
    public static final ResourceKey<ConfiguredFeature<?, ?>> SOUL_ROSE_HINT_CONFIGURED_FEATURE =
            ResourceKey.create(
                    Registries.CONFIGURED_FEATURE,
                    Identifier.fromNamespaceAndPath(CropCritters.MOD_ID, "soul_rose_hint")
            );
}
