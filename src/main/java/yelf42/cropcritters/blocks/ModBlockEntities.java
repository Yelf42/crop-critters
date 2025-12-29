package yelf42.cropcritters.blocks;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import yelf42.cropcritters.CropCritters;

public class ModBlockEntities {

    public static final BlockEntityType<MazewoodSaplingBlockEntity> MAZEWOOD_SAPLING = register(
            "mazewood_sapling",
            FabricBlockEntityTypeBuilder.create(MazewoodSaplingBlockEntity::new, ModBlocks.MAZEWOOD_SAPLING).build());

    public static final BlockEntityType<StrangleFernBlockEntity> STRANGLE_FERN = register(
            "strangle_fern",
            FabricBlockEntityTypeBuilder.create(StrangleFernBlockEntity::new, ModBlocks.STRANGLE_FERN).build());

    public static <T extends BlockEntityType<?>> T register(String path, T blockEntityType) {
        return Registry.register(Registries.BLOCK_ENTITY_TYPE, Identifier.of(CropCritters.MOD_ID, path), blockEntityType);
    }

    public static void initialize() {
        CropCritters.LOGGER.info("Initializing block entities for " + CropCritters.MOD_ID);
    }
}
