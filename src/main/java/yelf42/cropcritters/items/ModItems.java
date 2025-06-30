package yelf42.cropcritters.items;

import net.fabricmc.fabric.api.itemgroup.v1.*;
import net.fabricmc.fabric.api.registry.*;
import net.minecraft.item.*;
import net.minecraft.registry.*;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import yelf42.cropcritters.CropCritters;

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
                });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS)
                .register((itemGroup) -> {
                    itemGroup.add(ModItems.STRANGE_FERTILIZER);
                    itemGroup.add(ModItems.LOST_SOUL);
                });


        // Compostable
        CompostingChanceRegistry.INSTANCE.add(ModItems.STRANGE_FERTILIZER, 1.0f);

        // Fuel
        FuelRegistryEvents.BUILD.register((builder, context) -> {
            builder.add(ModItems.STRANGE_FERTILIZER, 80 * 20);
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
    public static final Item STRANGE_FERTILIZER = register("strange_fertilizer", StrangeFertilizerItem::new, new Item.Settings());
    public static final Item LOST_SOUL = register("lost_soul", Item::new, new Item.Settings());
}
