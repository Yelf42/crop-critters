package yelf42.cropcritters.entity;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
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
    }

    private static final RegistryKey<EntityType<?>> WHEAT_CRITTER_KEY = RegistryKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(CropCritters.MOD_ID,"wheat_critter"));
    public static final EntityType<WheatCritterEntity> WHEAT_CRITTER = Registry.register(Registries.ENTITY_TYPE,
            Identifier.of(CropCritters.MOD_ID, "wheat_critter"),
            EntityType.Builder.create(WheatCritterEntity::new, SpawnGroup.MISC)
                    .dimensions(1f, 1f).build(WHEAT_CRITTER_KEY));


}
