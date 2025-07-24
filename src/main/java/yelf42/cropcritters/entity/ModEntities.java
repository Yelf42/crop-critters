package yelf42.cropcritters.entity;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import yelf42.cropcritters.CropCritters;

public class ModEntities {

    public static void initialize() {
        CropCritters.LOGGER.info("Initializing entities for " + CropCritters.MOD_ID);

        FabricDefaultAttributeRegistry.register(ModEntities.WHEAT_CRITTER, WheatCritterEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.MELON_CRITTER, MelonCritterEntity.createAttributes());
    }

    private static final RegistryKey<EntityType<?>> SEED_BALL_PROJECTILE_KEY = RegistryKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(CropCritters.MOD_ID,"seed_ball_projectile"));
    public static final EntityType<SeedBallProjectileEntity> SEED_BALL_PROJECTILE = Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of(CropCritters.MOD_ID, "seed_ball_projectile"),
            FabricEntityTypeBuilder.<SeedBallProjectileEntity>create(SpawnGroup.MISC, SeedBallProjectileEntity::new)
                    .dimensions(EntityDimensions.fixed(0.25F, 0.25F))
                    .trackRangeBlocks(4).trackedUpdateRate(10)
                    .build(SEED_BALL_PROJECTILE_KEY)
    );

    private static final RegistryKey<EntityType<?>> SPIT_SEED_PROJECTILE_KEY = RegistryKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(CropCritters.MOD_ID,"seed_ball_projectile"));
    public static final EntityType<SpitSeedProjectileEntity> SPIT_SEED_PROJECTILE = Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of(CropCritters.MOD_ID, "spit_ball_projectile"),
            FabricEntityTypeBuilder.<SpitSeedProjectileEntity>create(SpawnGroup.MISC, SpitSeedProjectileEntity::new)
                    .dimensions(EntityDimensions.fixed(0.25F, 0.25F))
                    .trackRangeBlocks(4).trackedUpdateRate(10)
                    .build(SPIT_SEED_PROJECTILE_KEY)
    );

    private static final RegistryKey<EntityType<?>> WHEAT_CRITTER_KEY = RegistryKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(CropCritters.MOD_ID,"wheat_critter"));
    public static final EntityType<WheatCritterEntity> WHEAT_CRITTER = Registry.register(Registries.ENTITY_TYPE,
            Identifier.of(CropCritters.MOD_ID, "wheat_critter"),
            EntityType.Builder.create(WheatCritterEntity::new, SpawnGroup.MISC)
                    .dimensions(0.7f, 0.9f).build(WHEAT_CRITTER_KEY));

    private static final RegistryKey<EntityType<?>> MELON_CRITTER_KEY = RegistryKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(CropCritters.MOD_ID,"melon_critter"));
    public static final EntityType<MelonCritterEntity> MELON_CRITTER = Registry.register(Registries.ENTITY_TYPE,
            Identifier.of(CropCritters.MOD_ID, "melon_critter"),
            EntityType.Builder.create(MelonCritterEntity::new, SpawnGroup.MISC)
                    .dimensions(0.7f, 0.9f)
                    .eyeHeight(0.4f)
                    .build(MELON_CRITTER_KEY));


}
