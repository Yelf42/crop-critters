package yelf42.cropcritters.entity;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.Entity;
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
        FabricDefaultAttributeRegistry.register(ModEntities.PUMPKIN_CRITTER, PumpkinCritterEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.POTATO_CRITTER, PotatoCritterEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.CARROT_CRITTER, CarrotCritterEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.BEETROOT_CRITTER, BeetrootCritterEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.NETHER_WART_CRITTER, NetherWartCritterEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.POISONOUS_POTATO_CRITTER, PoisonousPotatoCritterEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.TORCHFLOWER_CRITTER, TorchflowerCritterEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.PITCHER_CRITTER, PitcherCritterEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.COCOA_CRITTER, CocoaCritterEntity.createAttributes());

    }

    public static final EntityType<SeedBallProjectileEntity> SEED_BALL_PROJECTILE = Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of(CropCritters.MOD_ID, "seed_ball_projectile"),
            FabricEntityTypeBuilder.<SeedBallProjectileEntity>create(SpawnGroup.MISC, SeedBallProjectileEntity::new)
                    .dimensions(EntityDimensions.fixed(0.25F, 0.25F))
                    .trackRangeBlocks(4).trackedUpdateRate(10)
                    .build(RegistryKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(CropCritters.MOD_ID,"seed_ball_projectile")))
    );

    public static final EntityType<SpitSeedProjectileEntity> SPIT_SEED_PROJECTILE = Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of(CropCritters.MOD_ID, "spit_ball_projectile"),
            FabricEntityTypeBuilder.<SpitSeedProjectileEntity>create(SpawnGroup.MISC, SpitSeedProjectileEntity::new)
                    .dimensions(EntityDimensions.fixed(0.125F, 0.125F))
                    .trackRangeBlocks(4).trackedUpdateRate(10)
                    .build(RegistryKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(CropCritters.MOD_ID,"seed_ball_projectile")))
    );

    public static final EntityType<MelonCritterEntity> MELON_CRITTER = registerCritter("melon_critter", MelonCritterEntity::new, 0.65f, 0.7f, 0.25f);
    public static final EntityType<PumpkinCritterEntity> PUMPKIN_CRITTER = registerCritter("pumpkin_critter", PumpkinCritterEntity::new, 0.65f, 0.65f, 0.25f);
    public static final EntityType<WheatCritterEntity> WHEAT_CRITTER = registerCritter("wheat_critter", WheatCritterEntity::new, 0.5f, 0.9f, 0.25f);
    public static final EntityType<CarrotCritterEntity> CARROT_CRITTER = registerCritter("carrot_critter", CarrotCritterEntity::new, 0.5f, 0.8f, 0.25f);
    public static final EntityType<PotatoCritterEntity> POTATO_CRITTER = registerCritter("potato_critter", PotatoCritterEntity::new, 0.5f, 0.6f, 0.25f);
    public static final EntityType<BeetrootCritterEntity> BEETROOT_CRITTER = registerCritter("beetroot_critter", BeetrootCritterEntity::new, 0.5f, 0.6f, 0.25f);
    public static final EntityType<NetherWartCritterEntity> NETHER_WART_CRITTER = registerCritter("nether_wart_critter", NetherWartCritterEntity::new, 0.3f, 0.5f, 0.15f);
    public static final EntityType<PoisonousPotatoCritterEntity> POISONOUS_POTATO_CRITTER = registerCritter("poisonous_potato_critter", PoisonousPotatoCritterEntity::new, 0.5f, 0.6f, 0.25f);
    public static final EntityType<TorchflowerCritterEntity> TORCHFLOWER_CRITTER = registerCritter("torchflower_critter", TorchflowerCritterEntity::new, 0.5f, 0.6f, 0.25f);
    public static final EntityType<PitcherCritterEntity> PITCHER_CRITTER = registerCritter("pitcher_critter", PitcherCritterEntity::new, 0.85f, 1.1f, 0.7f);
    public static final EntityType<CocoaCritterEntity> COCOA_CRITTER = registerCritter("cocoa_critter", CocoaCritterEntity::new, 0.5f, 0.75f, 0.25f);

    public static <T extends Entity> EntityType<T> registerCritter(String name, EntityType.EntityFactory<T> factory, float width, float height, float eyeHeight) {
        Identifier id = Identifier.of(CropCritters.MOD_ID, name);
        return Registry.register(
                Registries.ENTITY_TYPE,
                id,
                EntityType.Builder.create(factory, SpawnGroup.MISC)
                        .dimensions(width, height)
                        .eyeHeight(eyeHeight)
                        .build(RegistryKey.of(RegistryKeys.ENTITY_TYPE, id))
        );
    }

}
