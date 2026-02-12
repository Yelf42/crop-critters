package yelf42.cropcritters.items;

import net.fabricmc.fabric.api.itemgroup.v1.*;
import net.fabricmc.fabric.api.registry.*;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Rarity;
import yelf42.cropcritters.CropCritters;
import yelf42.cropcritters.entity.ModEntities;

import java.util.function.Function;

public class ModItems {
    public static void initialize() {
        CropCritters.LOGGER.info("Initializing items for " + CropCritters.MOD_ID);

        // Register items here
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, CROP_CRITTERS_ITEM_GROUP_KEY, CROP_CRITTERS_ITEM_GROUP);
        ItemGroupEvents.modifyEntriesEvent(CROP_CRITTERS_ITEM_GROUP_KEY)
                .register((itemGroup) -> {
                    itemGroup.accept(ModItems.STRANGE_FERTILIZER);
                    itemGroup.accept(ModItems.LOST_SOUL);
                    itemGroup.accept(ModItems.SEED_BALL);
                    itemGroup.accept(ModItems.WHEAT_CRITTER_SPAWN_EGG);
                    itemGroup.accept(ModItems.MELON_CRITTER_SPAWN_EGG);
                    itemGroup.accept(ModItems.PUMPKIN_CRITTER_SPAWN_EGG);
                    itemGroup.accept(ModItems.CARROT_CRITTER_SPAWN_EGG);
                    itemGroup.accept(ModItems.POTATO_CRITTER_SPAWN_EGG);
                    itemGroup.accept(ModItems.BEETROOT_CRITTER_SPAWN_EGG);
                    itemGroup.accept(ModItems.NETHER_WART_CRITTER_SPAWN_EGG);
                    itemGroup.accept(ModItems.POISONOUS_POTATO_CRITTER_SPAWN_EGG);
                    itemGroup.accept(ModItems.TORCHFLOWER_CRITTER_SPAWN_EGG);
                    itemGroup.accept(ModItems.PITCHER_CRITTER_SPAWN_EGG);
                    itemGroup.accept(ModItems.COCOA_CRITTER_SPAWN_EGG);
                    itemGroup.accept(ModItems.PUFFBOMB_SLICE);
                    itemGroup.accept(ModItems.COOKED_PUFFBOMB_STEAK);
                    itemGroup.accept(ModItems.SEED_BAR);
                    itemGroup.accept(ModItems.POPPER_POD);
                    itemGroup.accept(ModItems.HERBICIDE);
                });

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FOOD_AND_DRINKS)
                        .register((itemGroup) -> {
                            itemGroup.accept(ModItems.PUFFBOMB_SLICE);
                            itemGroup.accept(ModItems.COOKED_PUFFBOMB_STEAK);
                            itemGroup.accept(ModItems.SEED_BAR);
                        });

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.INGREDIENTS)
                .register((itemGroup) -> {
                    itemGroup.accept(ModItems.STRANGE_FERTILIZER);
                    itemGroup.accept(ModItems.LOST_SOUL);
                    itemGroup.accept(ModItems.PUFFBOMB_SLICE);
                });

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.SPAWN_EGGS)
                .register((itemGroup) -> {
                    itemGroup.accept(ModItems.WHEAT_CRITTER_SPAWN_EGG);
                    itemGroup.accept(ModItems.MELON_CRITTER_SPAWN_EGG);
                    itemGroup.accept(ModItems.PUMPKIN_CRITTER_SPAWN_EGG);
                    itemGroup.accept(ModItems.CARROT_CRITTER_SPAWN_EGG);
                    itemGroup.accept(ModItems.POTATO_CRITTER_SPAWN_EGG);
                    itemGroup.accept(ModItems.BEETROOT_CRITTER_SPAWN_EGG);
                    itemGroup.accept(ModItems.NETHER_WART_CRITTER_SPAWN_EGG);
                    itemGroup.accept(ModItems.POISONOUS_POTATO_CRITTER_SPAWN_EGG);
                    itemGroup.accept(ModItems.TORCHFLOWER_CRITTER_SPAWN_EGG);
                    itemGroup.accept(ModItems.PITCHER_CRITTER_SPAWN_EGG);
                    itemGroup.accept(ModItems.COCOA_CRITTER_SPAWN_EGG);
                });


        // Compostable
        CompostingChanceRegistry.INSTANCE.add(ModItems.STRANGE_FERTILIZER, 1.0f);
        CompostingChanceRegistry.INSTANCE.add(ModItems.SEED_BALL, 0.8f);
        CompostingChanceRegistry.INSTANCE.add(ModItems.SEED_BAR, 0.8f);
        CompostingChanceRegistry.INSTANCE.add(ModItems.PUFFBOMB_SLICE, 0.4f);

        // Fuel
        FuelRegistryEvents.BUILD.register((builder, context) -> {
            builder.add(ModItems.LOST_SOUL, 80 * 20);
        });

        // Dispenser behaviours
        DispenserBlock.registerProjectileBehavior(ModItems.SEED_BALL);

    }
    public static Item register(String name, Function<Item.Properties, Item> itemFactory, Item.Properties settings) {
        // Create the item key.
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(CropCritters.MOD_ID, name));

        // Create the item instance.
        Item item = itemFactory.apply(settings.setId(itemKey));

        // Register the item.
        Registry.register(BuiltInRegistries.ITEM, itemKey, item);

        return item;
    }
    public static final ResourceKey<CreativeModeTab> CROP_CRITTERS_ITEM_GROUP_KEY = ResourceKey.create(BuiltInRegistries.CREATIVE_MODE_TAB.key(), Identifier.fromNamespaceAndPath(CropCritters.MOD_ID, "item_group"));
    public static final CreativeModeTab CROP_CRITTERS_ITEM_GROUP = FabricItemGroup.builder()
            .icon(() -> new ItemStack(ModItems.LOST_SOUL))
            .title(Component.translatable("itemGroup.cropcritters"))
            .build();

    // Steps to making new item:
    // https://docs.fabricmc.net/develop/items/first-item#naming-the-item
    // 1. Add translation to en_us.json
    // 2. Add textures.item item.png
    // 3. Add items item.json
    // 4. Add models.items item.json
    public static final Item STRANGE_FERTILIZER = register("strange_fertilizer", StrangeFertilizerItem::new, new Item.Properties().rarity(Rarity.UNCOMMON));
    public static final Item LOST_SOUL = register("lost_soul", LostSoulItem::new, new Item.Properties().rarity(Rarity.UNCOMMON));

    public static final Item SEED_BALL = register("seed_ball", SeedBallItem::new, new Item.Properties().stacksTo(16).component(ModComponents.POISONOUS_SEED_BALL, new ModComponents.PoisonousComponent(0)));
    public static final Item POPPER_POD = register("popper_pod", PopperPodItem::new, new Item.Properties());
    public static final Item HERBICIDE = register("herbicide", HerbicideItem::new, new Item.Properties().stacksTo(16));

    // Foods
    public static final Item PUFFBOMB_SLICE = register("puffbomb_slice", Item::new, new Item.Properties().food((new FoodProperties.Builder()).nutrition(2).saturationModifier(0.4F).build()));
    public static final Item COOKED_PUFFBOMB_STEAK = register("cooked_puffbomb_steak", Item::new, new Item.Properties().food((new FoodProperties.Builder()).nutrition(7).saturationModifier(0.9F).build()));

    public static final Item SEED_BAR = register("seed_bar", Item::new, new Item.Properties().food((new FoodProperties.Builder()).nutrition(5).saturationModifier(0.7F).build()));


    // Spawn eggs
    public static Item registerSpawnEgg(String name, EntityType<? extends Mob> entityType) {
        // Create the item key.
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(CropCritters.MOD_ID, name));

        // Register the item.
        return Registry.register(BuiltInRegistries.ITEM,
                itemKey,
                new SpawnEggItem(new Item.Properties().setId(itemKey).spawnEgg(entityType)));
    }
    public static final Item WHEAT_CRITTER_SPAWN_EGG = registerSpawnEgg("wheat_critter_spawn_egg", ModEntities.WHEAT_CRITTER);
    public static final Item MELON_CRITTER_SPAWN_EGG = registerSpawnEgg("melon_critter_spawn_egg", ModEntities.MELON_CRITTER);
    public static final Item CARROT_CRITTER_SPAWN_EGG = registerSpawnEgg("carrot_critter_spawn_egg", ModEntities.CARROT_CRITTER);
    public static final Item PUMPKIN_CRITTER_SPAWN_EGG = registerSpawnEgg("pumpkin_critter_spawn_egg", ModEntities.PUMPKIN_CRITTER);
    public static final Item POTATO_CRITTER_SPAWN_EGG = registerSpawnEgg("potato_critter_spawn_egg", ModEntities.POTATO_CRITTER);
    public static final Item BEETROOT_CRITTER_SPAWN_EGG = registerSpawnEgg("beetroot_critter_spawn_egg", ModEntities.BEETROOT_CRITTER);
    public static final Item NETHER_WART_CRITTER_SPAWN_EGG = registerSpawnEgg("nether_wart_critter_spawn_egg", ModEntities.NETHER_WART_CRITTER);
    public static final Item POISONOUS_POTATO_CRITTER_SPAWN_EGG = registerSpawnEgg("poisonous_potato_critter_spawn_egg", ModEntities.POISONOUS_POTATO_CRITTER);
    public static final Item TORCHFLOWER_CRITTER_SPAWN_EGG = registerSpawnEgg("torchflower_critter_spawn_egg", ModEntities.TORCHFLOWER_CRITTER);
    public static final Item PITCHER_CRITTER_SPAWN_EGG = registerSpawnEgg("pitcher_critter_spawn_egg", ModEntities.PITCHER_CRITTER);
    public static final Item COCOA_CRITTER_SPAWN_EGG = registerSpawnEgg("cocoa_critter_spawn_egg", ModEntities.COCOA_CRITTER);

    // Custom recipe types
    public static final RecipeSerializer<SeedBallRecipe> SEED_BALL_RECIPE = Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, Identifier.fromNamespaceAndPath(CropCritters.MOD_ID, "crafting_special_seed_ball"), new CustomRecipe.Serializer<>(SeedBallRecipe::new));
    public static final RecipeSerializer<SeedBarRecipe> SEED_BAR_RECIPE = Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, Identifier.fromNamespaceAndPath(CropCritters.MOD_ID, "crafting_special_seed_bar"), new CustomRecipe.Serializer<>(SeedBarRecipe::new));

}
