package yelf42.cropcritters.blocks;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.registry.CompostingChanceRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.HugeMushroomBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.food.Foods;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Rarity;
import yelf42.cropcritters.CropCritters;
import yelf42.cropcritters.effects.ModEffects;
import yelf42.cropcritters.items.ModItems;

import java.util.function.Function;

public class ModBlocks {

    private static Block register(String name, Function<BlockBehaviour.Properties, Block> blockFactory, BlockBehaviour.Properties settings, boolean shouldRegisterItem) {
        // Create a registry key for the block
        ResourceKey<Block> blockKey = keyOfBlock(name);
        // Create the block instance
        Block block = blockFactory.apply(settings.setId(blockKey));

        // Sometimes, you may not want to register an item for the block.
        // Eg: if it's a technical block like `minecraft:moving_piston` or `minecraft:end_gateway`
        if (shouldRegisterItem) {
            // Items need to be registered with a different type of registry key, but the ID
            // can be the same.
            ResourceKey<Item> itemKey = keyOfItem(name);

            BlockItem blockItem = new BlockItem(block, new net.minecraft.world.item.Item.Properties().setId(itemKey));
            Registry.register(BuiltInRegistries.ITEM, itemKey, blockItem);
        }

        return Registry.register(BuiltInRegistries.BLOCK, blockKey, block);
    }

    private static Block register(String name, Function<BlockBehaviour.Properties, Block> blockFactory, BlockBehaviour.Properties settings, net.minecraft.world.item.Item.Properties itemSettings) {
        // Create a registry key for the block
        ResourceKey<Block> blockKey = keyOfBlock(name);
        // Create the block instance
        Block block = blockFactory.apply(settings.setId(blockKey));

        ResourceKey<Item> itemKey = keyOfItem(name);

        BlockItem blockItem = new BlockItem(block, itemSettings.setId(itemKey));
        Registry.register(BuiltInRegistries.ITEM, itemKey, blockItem);

        return Registry.register(BuiltInRegistries.BLOCK, blockKey, block);
    }

    private static Block registerPotted(String name, BlockBehaviour.Properties settings, Block flower) {
        // Create a registry key for the block
        ResourceKey<Block> blockKey = keyOfBlock(name);
        return Registry.register(BuiltInRegistries.BLOCK, blockKey, new FlowerPotBlock(flower, settings.setId(blockKey)));
    }

    private static ResourceKey<Block> keyOfBlock(String name) {
        return ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(CropCritters.MOD_ID, name));
    }

    private static ResourceKey<Item> keyOfItem(String name) {
        return ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(CropCritters.MOD_ID, name));
    }

    public static final Block SOUL_FARMLAND = register(
            "soul_farmland",
            SoulFarmland::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BROWN)
                    .strength(0.5F)
                    .sound(SoundType.SOUL_SOIL),
            true
    );

    public static final Block CRAWL_THISTLE = register(
            "crawl_thistle",
            CrawlThistle::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollision()
                    .randomTicks()
                    .strength(0.4f)
                    .sound(SoundType.SWEET_BERRY_BUSH)
                    .offsetType(BlockBehaviour.OffsetType.XZ)
                    .pushReaction(PushReaction.DESTROY),
            true
    );

    public static final Block MAZEWOOD_SAPLING = register(
            "mazewood_sapling",
            MazewoodSaplingBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollision()
                    .randomTicks()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .pushReaction(PushReaction.DESTROY),
            new net.minecraft.world.item.Item.Properties().rarity(Rarity.UNCOMMON)
    );
    public static final Block MAZEWOOD = register(
            "mazewood",
            MazewoodBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .forceSolidOn()
                    .strength(0.7f)
                    .sound(SoundType.SWEET_BERRY_BUSH)
                    .pushReaction(PushReaction.DESTROY),
            new net.minecraft.world.item.Item.Properties().rarity(Rarity.UNCOMMON)
    );

    public static final Block CRIMSON_THORNWEED = register(
            "crimson_thornweed",
            CrimsonThornweed::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.NETHER)
                    .noCollision()
                    .randomTicks()
                    .strength(0.6f)
                    .sound(SoundType.SWEET_BERRY_BUSH)
                    .offsetType(BlockBehaviour.OffsetType.XZ)
                    .pushReaction(PushReaction.DESTROY),
            true
    );

    public static final Block WITHERING_SPITEWEED = register(
            "withering_spiteweed",
            WitheringSpiteweed::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .noCollision()
                    .randomTicks()
                    .strength(0.6f)
                    .sound(SoundType.SWEET_BERRY_BUSH)
                    .offsetType(BlockBehaviour.OffsetType.XZ)
                    .pushReaction(PushReaction.DESTROY),
            true
    );

    public static final Block WAFTGRASS = register(
            "waftgrass",
            Waftgrass::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_CYAN)
                    .noCollision()
                    .randomTicks()
                    .strength(0.6f)
                    .sound(SoundType.SWEET_BERRY_BUSH)
                    .offsetType(BlockBehaviour.OffsetType.XZ)
                    .pushReaction(PushReaction.DESTROY),
            true
    );

    public static final Block STRANGLE_FERN = register(
            "strangle_fern",
            StrangleFern::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollision()
                    .randomTicks()
                    .strength(0.4f)
                    .sound(SoundType.SWEET_BERRY_BUSH)
                    .pushReaction(PushReaction.DESTROY),
            true
    );

    public static final Block POPPER_PLANT = register(
            "popper_plant",
            PopperPlantBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollision()
                    .randomTicks()
                    .strength(0.4f)
                    .sound(SoundType.SWEET_BERRY_BUSH)
                    .offsetType(BlockBehaviour.OffsetType.XZ)
                    .pushReaction(PushReaction.DESTROY),
            true
    );

    public static final Block BONE_TRAP = register(
            "bone_trap",
            BoneTrapBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOL)
                    .noCollision()
                    .strength(0.4f)
                    .sound(SoundType.BONE_BLOCK)
                    .pushReaction(PushReaction.DESTROY),
            true
    );

    public static final Block PUFFBOMB_MUSHROOM = register(
            "puffbomb_mushroom",
            PuffbombPlantBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOL)
                    .noCollision()
                    .randomTicks()
                    .instabreak()
                    .sound(SoundType.FUNGUS)
                    .pushReaction(PushReaction.DESTROY),
            new net.minecraft.world.item.Item.Properties().food(Foods.CARROT,
                    Consumable.builder()
                            .consumeSeconds(1.6F)
                            .animation(ItemUseAnimation.EAT)
                            .sound(SoundEvents.GENERIC_EAT)
                            .hasConsumeParticles(true)
                            .onConsume(new ApplyStatusEffectsConsumeEffect(ModEffects.EATEN_PUFFBOMB_POISONING))
                            .build())
    );

    public static final Block PUFFBOMB_MUSHROOM_BLOCK = register(
            "puffbomb_mushroom_block",
            HugeMushroomBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOL)
                    .instrument(NoteBlockInstrument.BASS)
                    .strength(0.2F)
                    .sound(SoundType.WOOD)
                    .ignitedByLava(),
            true
    );

    public static final Block LIVERWORT = register(
            "liverwort",
            LiverwortBlock::new,
            BlockBehaviour.Properties.of()
                    .replaceable()
                    .noCollision()
                    .instabreak()
                    .sound(SoundType.GLOW_LICHEN)
                    .ignitedByLava().pushReaction(PushReaction.DESTROY),
            true
    );

    public static final Block SOUL_ROSE = register(
            "soul_rose",
            SoulRoseBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.DIAMOND)
                    .noCollision()
                    .strength(0.9f)
                    .sound(SoundType.TWISTING_VINES)
                    .lightLevel((state) -> 3)
                    .pushReaction(PushReaction.DESTROY),
            new net.minecraft.world.item.Item.Properties().rarity(Rarity.RARE)
    );

    public static final Block TRIMMED_SOUL_ROSE = register(
            "trimmed_soul_rose",
            TrimmedSoulRoseBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.DIAMOND)
                    .noCollision()
                    .instabreak()
                    .sound(SoundType.TWISTING_VINES)
                    .lightLevel((state) -> 3)
                    .pushReaction(PushReaction.DESTROY),
            new net.minecraft.world.item.Item.Properties().rarity(Rarity.RARE)
    );

    public static final Block POTTED_SOUL_ROSE = registerPotted(
            "potted_soul_rose",
            BlockBehaviour.Properties.of()
                    .instabreak()
                    .lightLevel((state) -> 3)
                    .noOcclusion()
                    .pushReaction(PushReaction.DESTROY),
            SOUL_ROSE
    );

    public static final Block TALL_BUSH = register(
            "tall_bush",
            TallBushBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .replaceable()
                    .noCollision()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .ignitedByLava()
                    .pushReaction(PushReaction.DESTROY),
            true
    );

    public static final Block ORNAMENTAL_BUSH = register(
            "ornamental_bush",
            TallBushBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .replaceable()
                    .noCollision()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .ignitedByLava()
                    .pushReaction(PushReaction.DESTROY),
            true
    );

    public static final Block LOST_SOUL_IN_A_JAR = register(
            "lost_soul_in_a_jar",
            LostSoulInAJarBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .forceSolidOn()
                    .strength(0.3F)
                    .sound(SoundType.GLASS)
                    .lightLevel(state -> 12)
                    .noOcclusion()
                    .randomTicks()
                    .pushReaction(PushReaction.DESTROY),
            new net.minecraft.world.item.Item.Properties().rarity(Rarity.UNCOMMON)
    );

    public static final Block SOUL_POT = register(
            "soul_pot",
            SoulPotBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.TERRACOTTA_RED)
                    .strength(0.0F, 0.0F)
                    .pushReaction(PushReaction.DESTROY)
                    .noOcclusion(),
            true
    );

    public static final Block TORCHFLOWER_SPARK = register(
            "torchflower_spark",
            TorchflowerSparkBlock::new,
            BlockBehaviour.Properties.of()
                    .lightLevel((state) -> 12)
                    .replaceable()
                    .noCollision()
                    .noLootTable()
                    .randomTicks()
                    .air(),
            false
    );

    public static void initialize() {
        CropCritters.LOGGER.info("Initializing blocks for " + CropCritters.MOD_ID);

        ItemGroupEvents.modifyEntriesEvent(ModItems.CROP_CRITTERS_ITEM_GROUP_KEY).register((itemGroup) -> {
            itemGroup.accept(ModBlocks.SOUL_FARMLAND.asItem());
            itemGroup.accept(ModBlocks.MAZEWOOD_SAPLING.asItem());
            itemGroup.accept(ModBlocks.MAZEWOOD.asItem());
            itemGroup.accept(ModBlocks.CRAWL_THISTLE.asItem());
            itemGroup.accept(ModBlocks.CRIMSON_THORNWEED.asItem());
            itemGroup.accept(ModBlocks.WITHERING_SPITEWEED.asItem());
            itemGroup.accept(ModBlocks.WAFTGRASS.asItem());
            itemGroup.accept(ModBlocks.STRANGLE_FERN.asItem());
            itemGroup.accept(ModBlocks.POPPER_PLANT.asItem());
            itemGroup.accept(ModBlocks.BONE_TRAP.asItem());
            itemGroup.accept(ModBlocks.PUFFBOMB_MUSHROOM.asItem());
            itemGroup.accept(ModBlocks.PUFFBOMB_MUSHROOM_BLOCK.asItem());
            itemGroup.accept(ModBlocks.LIVERWORT.asItem());
            itemGroup.accept(ModBlocks.SOUL_ROSE.asItem());
            itemGroup.accept(ModBlocks.TRIMMED_SOUL_ROSE.asItem());
            itemGroup.accept(ModBlocks.TALL_BUSH.asItem());
            itemGroup.accept(ModBlocks.ORNAMENTAL_BUSH.asItem());
            itemGroup.accept(ModBlocks.LOST_SOUL_IN_A_JAR.asItem());
            itemGroup.accept(ModBlocks.SOUL_POT.asItem());
        });

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.NATURAL_BLOCKS).register((itemGroup) -> {
            itemGroup.accept(ModBlocks.MAZEWOOD_SAPLING.asItem());
            itemGroup.accept(ModBlocks.MAZEWOOD.asItem());
            itemGroup.accept(ModBlocks.CRAWL_THISTLE.asItem());
            itemGroup.accept(ModBlocks.CRIMSON_THORNWEED.asItem());
            itemGroup.accept(ModBlocks.WITHERING_SPITEWEED.asItem());
            itemGroup.accept(ModBlocks.WAFTGRASS.asItem());
            itemGroup.accept(ModBlocks.STRANGLE_FERN.asItem());
            itemGroup.accept(ModBlocks.BONE_TRAP.asItem());
            itemGroup.accept(ModBlocks.PUFFBOMB_MUSHROOM.asItem());
            itemGroup.accept(ModBlocks.PUFFBOMB_MUSHROOM_BLOCK.asItem());
            itemGroup.accept(ModBlocks.LIVERWORT.asItem());
            itemGroup.accept(ModBlocks.SOUL_ROSE.asItem());
            itemGroup.accept(ModBlocks.TRIMMED_SOUL_ROSE.asItem());
            itemGroup.accept(ModBlocks.TALL_BUSH.asItem());
            itemGroup.accept(ModBlocks.ORNAMENTAL_BUSH.asItem());
        });

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register((itemGroup) -> {
            itemGroup.accept(ModBlocks.SOUL_ROSE.asItem());
            itemGroup.accept(ModBlocks.LOST_SOUL_IN_A_JAR.asItem());
            itemGroup.accept(ModBlocks.SOUL_POT.asItem());
        });

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FOOD_AND_DRINKS)
                .register((itemGroup) -> {
                    itemGroup.accept(ModBlocks.PUFFBOMB_MUSHROOM.asItem());
                });

        CompostingChanceRegistry.INSTANCE.add(ModBlocks.TALL_BUSH.asItem(), 0.8f);
        CompostingChanceRegistry.INSTANCE.add(ModBlocks.ORNAMENTAL_BUSH.asItem(), 0.8f);
        CompostingChanceRegistry.INSTANCE.add(ModBlocks.MAZEWOOD.asItem(), 0.8f);
        CompostingChanceRegistry.INSTANCE.add(ModBlocks.PUFFBOMB_MUSHROOM.asItem(), 0.65f);
        CompostingChanceRegistry.INSTANCE.add(ModBlocks.PUFFBOMB_MUSHROOM_BLOCK.asItem(), 0.65f);
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
