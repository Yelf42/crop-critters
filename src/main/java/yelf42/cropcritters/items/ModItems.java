package yelf42.cropcritters.items;

import net.fabricmc.fabric.api.itemgroup.v1.*;
import net.fabricmc.fabric.api.registry.*;
import net.minecraft.block.DispenserBlock;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.*;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.registry.*;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import yelf42.cropcritters.CropCritters;
import yelf42.cropcritters.entity.ModEntities;

import java.util.function.Function;

public class ModItems {
    public static void initialize() {
        CropCritters.LOGGER.info("Initializing items for " + CropCritters.MOD_ID);

        // Register items here
        Registry.register(Registries.ITEM_GROUP, CROP_CRITTERS_ITEM_GROUP_KEY, CROP_CRITTERS_ITEM_GROUP);
        ItemGroupEvents.modifyEntriesEvent(CROP_CRITTERS_ITEM_GROUP_KEY)
                .register((itemGroup) -> {
                    itemGroup.add(ModItems.STRANGE_FERTILIZER);
                    itemGroup.add(ModItems.LOST_SOUL);
                    itemGroup.add(ModItems.SEED_BALL);
                    itemGroup.add(ModItems.WHEAT_CRITTER_SPAWN_EGG);
                    itemGroup.add(ModItems.MELON_CRITTER_SPAWN_EGG);
                    itemGroup.add(ModItems.PUMPKIN_CRITTER_SPAWN_EGG);
                    itemGroup.add(ModItems.CARROT_CRITTER_SPAWN_EGG);
                    itemGroup.add(ModItems.POTATO_CRITTER_SPAWN_EGG);
                    itemGroup.add(ModItems.BEETROOT_CRITTER_SPAWN_EGG);
                    itemGroup.add(ModItems.NETHER_WART_CRITTER_SPAWN_EGG);
                    itemGroup.add(ModItems.POISONOUS_POTATO_CRITTER_SPAWN_EGG);
                    itemGroup.add(ModItems.TORCHFLOWER_CRITTER_SPAWN_EGG);
                    itemGroup.add(ModItems.PITCHER_CRITTER_SPAWN_EGG);
                    itemGroup.add(ModItems.COCOA_CRITTER_SPAWN_EGG);
                    itemGroup.add(ModItems.PUFFBOMB_SLICE);
                    itemGroup.add(ModItems.COOKED_PUFFBOMB_STEAK);
                    itemGroup.add(ModItems.POPPER_POD);
                    itemGroup.add(ModItems.HERBICIDE);
                });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS)
                .register((itemGroup) -> {
                    itemGroup.add(ModItems.STRANGE_FERTILIZER);
                    itemGroup.add(ModItems.LOST_SOUL);
                    itemGroup.add(ModItems.PUFFBOMB_SLICE);
                });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS)
                .register((itemGroup) -> {
                    itemGroup.add(ModItems.WHEAT_CRITTER_SPAWN_EGG);
                    itemGroup.add(ModItems.MELON_CRITTER_SPAWN_EGG);
                    itemGroup.add(ModItems.PUMPKIN_CRITTER_SPAWN_EGG);
                    itemGroup.add(ModItems.CARROT_CRITTER_SPAWN_EGG);
                    itemGroup.add(ModItems.POTATO_CRITTER_SPAWN_EGG);
                    itemGroup.add(ModItems.BEETROOT_CRITTER_SPAWN_EGG);
                    itemGroup.add(ModItems.NETHER_WART_CRITTER_SPAWN_EGG);
                    itemGroup.add(ModItems.POISONOUS_POTATO_CRITTER_SPAWN_EGG);
                    itemGroup.add(ModItems.TORCHFLOWER_CRITTER_SPAWN_EGG);
                    itemGroup.add(ModItems.PITCHER_CRITTER_SPAWN_EGG);
                    itemGroup.add(ModItems.COCOA_CRITTER_SPAWN_EGG);
                });


        // Compostable
        CompostingChanceRegistry.INSTANCE.add(ModItems.STRANGE_FERTILIZER, 1.0f);
        CompostingChanceRegistry.INSTANCE.add(ModItems.SEED_BALL, 0.8f);
        CompostingChanceRegistry.INSTANCE.add(ModItems.PUFFBOMB_SLICE, 0.4f);

        // Fuel
        FuelRegistryEvents.BUILD.register((builder, context) -> {
            builder.add(ModItems.LOST_SOUL, 80 * 20);
        });

        // Dispenser behaviours
        DispenserBlock.registerProjectileBehavior(ModItems.SEED_BALL);

    }
    public static Item register(String name, Function<Item.Settings, Item> itemFactory, Item.Settings settings) {
        // Create the item key.
        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(CropCritters.MOD_ID, name));

        // Create the item instance.
        Item item = itemFactory.apply(settings.registryKey(itemKey));

        // Register the item.
        Registry.register(Registries.ITEM, itemKey, item);

        return item;
    }
    public static final RegistryKey<ItemGroup> CROP_CRITTERS_ITEM_GROUP_KEY = RegistryKey.of(Registries.ITEM_GROUP.getKey(), Identifier.of(CropCritters.MOD_ID, "item_group"));
    public static final ItemGroup CROP_CRITTERS_ITEM_GROUP = FabricItemGroup.builder()
            .icon(() -> new ItemStack(ModItems.LOST_SOUL))
            .displayName(Text.translatable("itemGroup.cropcritters"))
            .build();

    // Steps to making new item:
    // https://docs.fabricmc.net/develop/items/first-item#naming-the-item
    // 1. Add translation to en_us.json
    // 2. Add textures.item item.png
    // 3. Add items item.json
    // 4. Add models.items item.json
    public static final Item STRANGE_FERTILIZER = register("strange_fertilizer", StrangeFertilizerItem::new, new Item.Settings().rarity(Rarity.UNCOMMON));
    public static final Item LOST_SOUL = register("lost_soul", LostSoulItem::new, new Item.Settings().rarity(Rarity.UNCOMMON));

    public static final Item SEED_BALL = register("seed_ball", SeedBallItem::new, new Item.Settings().maxCount(16).component(ModComponents.POISONOUS_SEED_BALL, new ModComponents.PoisonousComponent(0)));
    public static final Item POPPER_POD = register("popper_pod", PopperPodItem::new, new Item.Settings());
    public static final Item HERBICIDE = register("herbicide", HerbicideItem::new, new Item.Settings().maxCount(16));

    // Foods
    public static final Item PUFFBOMB_SLICE = register("puffbomb_slice", Item::new, new Item.Settings().food((new FoodComponent.Builder()).nutrition(2).saturationModifier(0.4F).build()));
    public static final Item COOKED_PUFFBOMB_STEAK = register("cooked_puffbomb_steak", Item::new, new Item.Settings().food((new FoodComponent.Builder()).nutrition(7).saturationModifier(0.9F).build()));


    // Spawn eggs
    public static Item registerSpawnEgg(String name, EntityType<? extends MobEntity> entityType) {
        // Create the item key.
        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(CropCritters.MOD_ID, name));

        // Register the item.
        return Registry.register(Registries.ITEM,
                itemKey,
                new SpawnEggItem(new Item.Settings().registryKey(itemKey).spawnEgg(entityType)));
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
    public static final RecipeSerializer<SeedBallRecipe> SEED_BALL_RECIPE = Registry.register(Registries.RECIPE_SERIALIZER, Identifier.of(CropCritters.MOD_ID, "crafting_special_seed_ball"), new SpecialCraftingRecipe.SpecialRecipeSerializer<>(SeedBallRecipe::new));

}
