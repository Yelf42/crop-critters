package yelf42.cropcritters.items;

import net.fabricmc.fabric.api.itemgroup.v1.*;
import net.fabricmc.fabric.api.registry.*;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.*;
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
                });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS)
                .register((itemGroup) -> {
                    itemGroup.add(ModItems.STRANGE_FERTILIZER);
                    itemGroup.add(ModItems.LOST_SOUL);
                });


        // Compostable
        CompostingChanceRegistry.INSTANCE.add(ModItems.STRANGE_FERTILIZER, 1.0f);
        CompostingChanceRegistry.INSTANCE.add(ModItems.SEED_BALL, 0.8f);

        // Fuel
        FuelRegistryEvents.BUILD.register((builder, context) -> {
            builder.add(ModItems.LOST_SOUL, 80 * 20);
        });
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
    public static final Item SEED_BALL = register("seed_ball", SeedBallItem::new, new Item.Settings().maxCount(16));



    public static Item registerSpawnEgg(String name, EntityType<? extends MobEntity> entityType) {
        // Create the item key.
        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(CropCritters.MOD_ID, name));

        // Register the item.
        return Registry.register(Registries.ITEM,
                itemKey,
                new SpawnEggItem(entityType, new Item.Settings().registryKey(itemKey)));
    }
    public static final Item WHEAT_CRITTER_SPAWN_EGG = registerSpawnEgg("wheat_critter_spawn_egg", ModEntities.WHEAT_CRITTER);
    public static final Item MELON_CRITTER_SPAWN_EGG = registerSpawnEgg("melon_critter_spawn_egg", ModEntities.MELON_CRITTER);
    public static final Item CARROT_CRITTER_SPAWN_EGG = registerSpawnEgg("carrot_critter_spawn_egg", ModEntities.CARROT_CRITTER);
    public static final Item PUMPKIN_CRITTER_SPAWN_EGG = registerSpawnEgg("pumpkin_critter_spawn_egg", ModEntities.PUMPKIN_CRITTER);
    public static final Item POTATO_CRITTER_SPAWN_EGG = registerSpawnEgg("potato_critter_spawn_egg", ModEntities.POTATO_CRITTER);
    public static final Item BEETROOT_CRITTER_SPAWN_EGG = registerSpawnEgg("beetroot_critter_spawn_egg", ModEntities.BEETROOT_CRITTER);
    public static final Item NETHER_WART_CRITTER_SPAWN_EGG = registerSpawnEgg("nether_wart_critter_spawn_egg", ModEntities.NETHER_WART_CRITTER);
    public static final Item POISONOUS_POTATO_CRITTER_SPAWN_EGG = registerSpawnEgg("poisonous_potato_critter_spawn_egg", ModEntities.POISONOUS_POTATO_CRITTER);




}
