package yelf42.cropcritters.blocks;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.*;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.*;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import yelf42.cropcritters.CropCritters;
import yelf42.cropcritters.items.ModItems;

import java.util.function.Function;

public class ModBlocks {

    private static Block register(String name, Function<AbstractBlock.Settings, Block> blockFactory, AbstractBlock.Settings settings, boolean shouldRegisterItem) {
        // Create a registry key for the block
        RegistryKey<Block> blockKey = keyOfBlock(name);
        // Create the block instance
        Block block = blockFactory.apply(settings.registryKey(blockKey));

        // Sometimes, you may not want to register an item for the block.
        // Eg: if it's a technical block like `minecraft:moving_piston` or `minecraft:end_gateway`
        if (shouldRegisterItem) {
            // Items need to be registered with a different type of registry key, but the ID
            // can be the same.
            RegistryKey<Item> itemKey = keyOfItem(name);

            BlockItem blockItem = new BlockItem(block, new Item.Settings().registryKey(itemKey));
            Registry.register(Registries.ITEM, itemKey, blockItem);
        }

        return Registry.register(Registries.BLOCK, blockKey, block);
    }

    public static final Block SOUL_FARMLAND = register(
            "soul_farmland",
            SoulFarmland::new,
            AbstractBlock.Settings.create()
                    .mapColor(MapColor.BROWN)
                    .strength(0.5F)
                    .sounds(BlockSoundGroup.SOUL_SOIL),
            true
    );

    public static final Block CRAWL_THISTLE = register(
            "crawl_thistle",
            CrawlThistle::new,
            AbstractBlock.Settings.create()
                    .mapColor(MapColor.DARK_GREEN)
                    .noCollision()
                    .requiresTool()
                    .ticksRandomly()
                    .strength(0.5f)
                    .sounds(BlockSoundGroup.SWEET_BERRY_BUSH)
                    .offset(AbstractBlock.OffsetType.XZ)
                    .pistonBehavior(PistonBehavior.DESTROY),
            true
    );

    public static final Block CRIMSON_THORNWEED = register(
            "crimson_thornweed",
            CrimsonThornweed::new,
            AbstractBlock.Settings.create()
                    .mapColor(MapColor.DARK_RED)
                    .noCollision()
                    .requiresTool()
                    .ticksRandomly()
                    .strength(0.5f)
                    .sounds(BlockSoundGroup.SWEET_BERRY_BUSH)
                    .offset(AbstractBlock.OffsetType.XZ)
                    .pistonBehavior(PistonBehavior.DESTROY),
            true
    );

    public static final Block WITHERING_SPITEWEED = register(
            "withering_spiteweed",
            WitheringSpiteweed::new,
            AbstractBlock.Settings.create()
                    .mapColor(MapColor.BLACK)
                    .noCollision()
                    .requiresTool()
                    .ticksRandomly()
                    .strength(0.5f)
                    .sounds(BlockSoundGroup.SWEET_BERRY_BUSH)
                    .offset(AbstractBlock.OffsetType.XZ)
                    .pistonBehavior(PistonBehavior.DESTROY),
            true
    );

    public static final Block TALL_BUSH = register(
            "tall_bush",
            TallBushBlock::new,
            AbstractBlock.Settings.create()
                    .mapColor(MapColor.DARK_GREEN)
                    .replaceable()
                    .noCollision()
                    .breakInstantly()
                    .sounds(BlockSoundGroup.GRASS)
                    .burnable()
                    .pistonBehavior(PistonBehavior.DESTROY),
            true
    );

    public static final Block ORNAMENTAL_BUSH = register(
            "ornamental_bush",
            TallPlantBlock::new,
            AbstractBlock.Settings.create()
                    .mapColor(MapColor.DARK_GREEN)
                    .replaceable()
                    .noCollision()
                    .breakInstantly()
                    .sounds(BlockSoundGroup.GRASS)
                    .burnable()
                    .pistonBehavior(PistonBehavior.DESTROY),
            true
    );

    public static final Block LOST_SOUL_IN_A_JAR = register(
            "lost_soul_in_a_jar",
            LostSoulInAJarBlock::new,
            AbstractBlock.Settings.create()
                    .mapColor(MapColor.IRON_GRAY)
                    .solid()
                    .strength(2.5F)
                    .sounds(BlockSoundGroup.GLASS)
                    .luminance(state -> 15)
                    .nonOpaque()
                    .ticksRandomly()
                    .pistonBehavior(PistonBehavior.DESTROY),
            true
    );

    private static RegistryKey<Block> keyOfBlock(String name) {
        return RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(CropCritters.MOD_ID, name));
    }

    private static RegistryKey<Item> keyOfItem(String name) {
        return RegistryKey.of(RegistryKeys.ITEM, Identifier.of(CropCritters.MOD_ID, name));
    }

    public static void initialize() {
        CropCritters.LOGGER.info("Initializing blocks for " + CropCritters.MOD_ID);

        ItemGroupEvents.modifyEntriesEvent(ModItems.CROP_CRITTERS_ITEM_GROUP_KEY).register((itemGroup) -> {
            itemGroup.add(ModBlocks.SOUL_FARMLAND.asItem());
            itemGroup.add(ModBlocks.CRAWL_THISTLE.asItem());
            itemGroup.add(ModBlocks.CRIMSON_THORNWEED.asItem());
            itemGroup.add(ModBlocks.WITHERING_SPITEWEED.asItem());
            itemGroup.add(ModBlocks.TALL_BUSH.asItem());
            itemGroup.add(ModBlocks.ORNAMENTAL_BUSH.asItem());
            itemGroup.add(ModBlocks.LOST_SOUL_IN_A_JAR.asItem());
        });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.NATURAL).register((itemGroup) -> {
            itemGroup.add(ModBlocks.CRAWL_THISTLE.asItem());
            itemGroup.add(ModBlocks.CRIMSON_THORNWEED.asItem());
            itemGroup.add(ModBlocks.WITHERING_SPITEWEED.asItem());
            itemGroup.add(ModBlocks.TALL_BUSH.asItem());
            itemGroup.add(ModBlocks.ORNAMENTAL_BUSH.asItem());
        });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register((itemGroup) -> {
            itemGroup.add(ModBlocks.LOST_SOUL_IN_A_JAR.asItem());
        });

    }
}
