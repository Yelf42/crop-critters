package yelf42.cropcritters.blocks;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import yelf42.cropcritters.CropCritters;

public class ModBlockEntities {

    public static final BlockEntityType<MazewoodSaplingBlockEntity> MAZEWOOD_SAPLING = register(
            "mazewood_sapling",
            FabricBlockEntityTypeBuilder.create(MazewoodSaplingBlockEntity::new, ModBlocks.MAZEWOOD_SAPLING).build());

    public static final BlockEntityType<StrangleFernBlockEntity> STRANGLE_FERN = register(
            "strangle_fern",
            FabricBlockEntityTypeBuilder.create(StrangleFernBlockEntity::new, ModBlocks.STRANGLE_FERN).build());

    public static final BlockEntityType<SoulRoseBlockEntity> SOUL_ROSE = register(
            "soul_rose",
            FabricBlockEntityTypeBuilder.create(SoulRoseBlockEntity::new, ModBlocks.SOUL_ROSE).build());

    public static BlockEntityType<SoulPotBlockEntity> SOUL_POT = register(
            "soul_pot",
            FabricBlockEntityTypeBuilder.create(SoulPotBlockEntity::new, ModBlocks.SOUL_POT).build());

    public static <T extends BlockEntityType<?>> T register(String path, T blockEntityType) {
        return Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Identifier.fromNamespaceAndPath(CropCritters.MOD_ID, path), blockEntityType);
    }

    public static void initialize() {
        CropCritters.LOGGER.info("Initializing block entities for " + CropCritters.MOD_ID);
    }
}
