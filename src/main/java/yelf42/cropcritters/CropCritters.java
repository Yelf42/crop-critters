package yelf42.cropcritters;

import net.fabricmc.api.ModInitializer;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yelf42.cropcritters.blocks.ModBlocks;
import yelf42.cropcritters.events.ModEvents;
import yelf42.cropcritters.items.ModItems;

public class CropCritters implements ModInitializer {
	public static final String MOD_ID = "cropcritters";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final TagKey<EntityType<?>> WEED_IMMUNE = TagKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of("cropcritters", "weed_immune"));
	public static final TagKey<EntityType<?>> CROP_CRITTERS = TagKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of("cropcritters", "crop_critters"));
	public static final TagKey<Block> UNDERWATER_STRANGE_FERTILIZERS = TagKey.of(RegistryKeys.BLOCK, Identifier.of("cropcritters", "underwater_strange_fertilizers"));
	public static final TagKey<Block> ON_LAND_COMMON_STRANGE_FERTILIZERS = TagKey.of(RegistryKeys.BLOCK, Identifier.of("cropcritters", "on_land_common_strange_fertilizers"));
	public static final TagKey<Block> ON_LAND_RARE_STRANGE_FERTILIZERS = TagKey.of(RegistryKeys.BLOCK, Identifier.of("cropcritters", "on_land_rare_strange_fertilizers"));
	public static final TagKey<Block> ON_NYLIUM_STRANGE_FERTILIZERS = TagKey.of(RegistryKeys.BLOCK, Identifier.of("cropcritters", "on_nylium_strange_fertilizers"));
	public static final TagKey<Block> IGNORE_STRANGE_FERTILIZERS = TagKey.of(RegistryKeys.BLOCK, Identifier.of("cropcritters", "ignore_strange_fertilizers"));


	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("Starting initialize of " + CropCritters.MOD_ID);
		ModItems.initialize();
		ModBlocks.initialize();
		ModEvents.initialize();
	}
}