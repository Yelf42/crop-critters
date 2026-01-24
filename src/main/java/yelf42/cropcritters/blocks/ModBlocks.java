package yelf42.cropcritters.blocks;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.registry.CompostingChanceRegistry;
import net.minecraft.block.*;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.component.type.ConsumableComponent;
import net.minecraft.component.type.FoodComponents;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.consume.ApplyEffectsConsumeEffect;
import net.minecraft.item.consume.UseAction;
import net.minecraft.registry.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.event.GameEvent;
import yelf42.cropcritters.CropCritters;
import yelf42.cropcritters.config.ConfigManager;
import yelf42.cropcritters.effects.ModEffects;
import yelf42.cropcritters.items.ModItems;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static net.minecraft.block.Block.pushEntitiesUpBeforeBlockChange;

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

    private static Block register(String name, Function<AbstractBlock.Settings, Block> blockFactory, AbstractBlock.Settings settings, boolean shouldRegisterItem, Item.Settings itemSettings) {
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

            BlockItem blockItem = new BlockItem(block, itemSettings.registryKey(itemKey));
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
                    .ticksRandomly()
                    .strength(0.4f)
                    .sounds(BlockSoundGroup.SWEET_BERRY_BUSH)
                    .offset(AbstractBlock.OffsetType.XZ)
                    .pistonBehavior(PistonBehavior.DESTROY),
            true
    );

    public static final Block MAZEWOOD_SAPLING = register(
            "mazewood_sapling",
            MazewoodSaplingBlock::new,
            AbstractBlock.Settings.create()
                    .mapColor(MapColor.DARK_GREEN)
                    .noCollision()
                    .ticksRandomly()
                    .breakInstantly()
                    .sounds(BlockSoundGroup.GRASS)
                    .pistonBehavior(PistonBehavior.DESTROY),
            true
    );
    public static final Block MAZEWOOD = register(
            "mazewood",
            MazewoodBlock::new,
            AbstractBlock.Settings.create()
                    .mapColor(MapColor.DARK_GREEN)
                    .solid()
                    .strength(0.7f)
                    .sounds(BlockSoundGroup.SWEET_BERRY_BUSH)
                    .pistonBehavior(PistonBehavior.DESTROY),
            true
    );

    public static final Block CRIMSON_THORNWEED = register(
            "crimson_thornweed",
            CrimsonThornweed::new,
            AbstractBlock.Settings.create()
                    .mapColor(MapColor.DARK_RED)
                    .noCollision()
                    .ticksRandomly()
                    .strength(0.6f)
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
                    .ticksRandomly()
                    .strength(0.6f)
                    .sounds(BlockSoundGroup.SWEET_BERRY_BUSH)
                    .offset(AbstractBlock.OffsetType.XZ)
                    .pistonBehavior(PistonBehavior.DESTROY),
            true
    );

    public static final Block WAFTGRASS = register(
            "waftgrass",
            Waftgrass::new,
            AbstractBlock.Settings.create()
                    .mapColor(MapColor.CYAN)
                    .noCollision()
                    .ticksRandomly()
                    .strength(0.6f)
                    .sounds(BlockSoundGroup.SWEET_BERRY_BUSH)
                    .offset(AbstractBlock.OffsetType.XZ)
                    .pistonBehavior(PistonBehavior.DESTROY),
            true
    );

    public static final Block STRANGLE_FERN = register(
            "strangle_fern",
            StrangleFern::new,
            AbstractBlock.Settings.create()
                    .mapColor(MapColor.DARK_GREEN)
                    .noCollision()
                    .ticksRandomly()
                    .strength(0.4f)
                    .sounds(BlockSoundGroup.SWEET_BERRY_BUSH)
                    .pistonBehavior(PistonBehavior.DESTROY),
            true
    );

    public static final Block POPPER_PLANT = register(
            "popper_plant",
            PopperPlantBlock::new,
            AbstractBlock.Settings.create()
                    .mapColor(MapColor.DARK_GREEN)
                    .noCollision()
                    .ticksRandomly()
                    .strength(0.4f)
                    .sounds(BlockSoundGroup.SWEET_BERRY_BUSH)
                    .pistonBehavior(PistonBehavior.DESTROY),
            true
    );

    public static final Block BONE_TRAP = register(
            "bone_trap",
            BoneTrapBlock::new,
            AbstractBlock.Settings.create()
                    .mapColor(MapColor.WHITE_GRAY)
                    .noCollision()
                    .strength(0.4f)
                    .sounds(BlockSoundGroup.BONE)
                    .pistonBehavior(PistonBehavior.DESTROY),
            true
    );

    public static final Block PUFFBOMB_MUSHROOM = register(
            "puffbomb_mushroom",
            PuffbombPlantBlock::new,
            AbstractBlock.Settings.create()
                    .mapColor(MapColor.WHITE_GRAY)
                    .noCollision()
                    .ticksRandomly()
                    .breakInstantly()
                    .sounds(BlockSoundGroup.GRASS)
                    .pistonBehavior(PistonBehavior.DESTROY),
            true,
            new Item.Settings().food(FoodComponents.CARROT,
                    ConsumableComponent.builder()
                            .consumeSeconds(1.6F)
                            .useAction(UseAction.EAT)
                            .sound(SoundEvents.ENTITY_GENERIC_EAT)
                            .consumeParticles(true)
                            .consumeEffect(new ApplyEffectsConsumeEffect(ModEffects.EATEN_PUFFBOMB_POISONING))
                            .build())
    );

    public static final Block PUFFBOMB_MUSHROOM_BLOCK = register(
            "puffbomb_mushroom_block",
            MushroomBlock::new,
            AbstractBlock.Settings.create()
                    .mapColor(MapColor.WHITE_GRAY)
                    .instrument(NoteBlockInstrument.BASS)
                    .strength(0.2F).sounds(BlockSoundGroup.WOOD)
                    .burnable(),
            true
    );

    public static final Block LIVERWORT = register(
            "liverwort",
            LiverwortBlock::new,
            AbstractBlock.Settings.create()
                    .replaceable()
                    .noCollision()
                    .breakInstantly()
                    .sounds(BlockSoundGroup.GLOW_LICHEN)
                    .burnable().pistonBehavior(PistonBehavior.DESTROY),
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

    public static final Block LOST_SOUL_IN_A_JAR = register(
            "lost_soul_in_a_jar",
            LostSoulInAJarBlock::new,
            AbstractBlock.Settings.create()
                    .mapColor(MapColor.IRON_GRAY)
                    .solid()
                    .strength(0.3F)
                    .sounds(BlockSoundGroup.GLASS)
                    .luminance(state -> 12)
                    .nonOpaque()
                    .ticksRandomly()
                    .pistonBehavior(PistonBehavior.DESTROY),
            true
    );

    public static final Block TORCHFLOWER_SPARK = register(
            "torchflower_spark",
            TorchflowerSparkBlock::new,
            AbstractBlock.Settings.create()
                    .luminance((state) -> 12)
                    .replaceable()
                    .noCollision()
                    .dropsNothing()
                    .ticksRandomly()
                    .air(),
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
            itemGroup.add(ModBlocks.MAZEWOOD_SAPLING.asItem());
            itemGroup.add(ModBlocks.MAZEWOOD.asItem());
            itemGroup.add(ModBlocks.CRAWL_THISTLE.asItem());
            itemGroup.add(ModBlocks.CRIMSON_THORNWEED.asItem());
            itemGroup.add(ModBlocks.WITHERING_SPITEWEED.asItem());
            itemGroup.add(ModBlocks.WAFTGRASS.asItem());
            itemGroup.add(ModBlocks.STRANGLE_FERN.asItem());
            itemGroup.add(ModBlocks.POPPER_PLANT.asItem());
            itemGroup.add(ModBlocks.BONE_TRAP.asItem());
            itemGroup.add(ModBlocks.PUFFBOMB_MUSHROOM.asItem());
            itemGroup.add(ModBlocks.PUFFBOMB_MUSHROOM_BLOCK.asItem());
            itemGroup.add(ModBlocks.LIVERWORT.asItem());
            itemGroup.add(ModBlocks.TALL_BUSH.asItem());
            itemGroup.add(ModBlocks.ORNAMENTAL_BUSH.asItem());
            itemGroup.add(ModBlocks.LOST_SOUL_IN_A_JAR.asItem());
        });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.NATURAL).register((itemGroup) -> {
            itemGroup.add(ModBlocks.MAZEWOOD_SAPLING.asItem());
            itemGroup.add(ModBlocks.MAZEWOOD.asItem());
            itemGroup.add(ModBlocks.CRAWL_THISTLE.asItem());
            itemGroup.add(ModBlocks.CRIMSON_THORNWEED.asItem());
            itemGroup.add(ModBlocks.WITHERING_SPITEWEED.asItem());
            itemGroup.add(ModBlocks.WAFTGRASS.asItem());
            itemGroup.add(ModBlocks.STRANGLE_FERN.asItem());
            itemGroup.add(ModBlocks.BONE_TRAP.asItem());
            itemGroup.add(ModBlocks.PUFFBOMB_MUSHROOM.asItem());
            itemGroup.add(ModBlocks.PUFFBOMB_MUSHROOM_BLOCK.asItem());
            itemGroup.add(ModBlocks.LIVERWORT.asItem());
            itemGroup.add(ModBlocks.TALL_BUSH.asItem());
            itemGroup.add(ModBlocks.ORNAMENTAL_BUSH.asItem());
        });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register((itemGroup) -> {
            itemGroup.add(ModBlocks.LOST_SOUL_IN_A_JAR.asItem());
        });

        CompostingChanceRegistry.INSTANCE.add(ModBlocks.TALL_BUSH.asItem(), 0.8f);
        CompostingChanceRegistry.INSTANCE.add(ModBlocks.ORNAMENTAL_BUSH.asItem(), 0.8f);
        CompostingChanceRegistry.INSTANCE.add(ModBlocks.MAZEWOOD.asItem(), 0.8f);
        CompostingChanceRegistry.INSTANCE.add(ModBlocks.PUFFBOMB_MUSHROOM.asItem(), 0.65f);
        CompostingChanceRegistry.INSTANCE.add(ModBlocks.BONE_TRAP.asItem(), 0.6f);
        CompostingChanceRegistry.INSTANCE.add(ModBlocks.MAZEWOOD_SAPLING.asItem(), 0.4f);
        CompostingChanceRegistry.INSTANCE.add(ModBlocks.CRAWL_THISTLE.asItem(), 0.3f);
        CompostingChanceRegistry.INSTANCE.add(ModBlocks.CRIMSON_THORNWEED.asItem(), 0.2f);
        CompostingChanceRegistry.INSTANCE.add(ModBlocks.STRANGLE_FERN.asItem(), 0.2f);
        CompostingChanceRegistry.INSTANCE.add(ModBlocks.POPPER_PLANT.asItem(), 0.2f);
        CompostingChanceRegistry.INSTANCE.add(ModBlocks.LIVERWORT.asItem(), 0.2f);
        CompostingChanceRegistry.INSTANCE.add(ModBlocks.WAFTGRASS.asItem(), 0.2f);
        CompostingChanceRegistry.INSTANCE.add(ModBlocks.WITHERING_SPITEWEED.asItem(), 0f);


    }
}
