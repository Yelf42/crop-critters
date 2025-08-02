package yelf42.cropcritters;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.gen.GenerationStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yelf42.cropcritters.blocks.ModBlocks;
import yelf42.cropcritters.config.ConfigManager;
import yelf42.cropcritters.entity.ModEntities;
import yelf42.cropcritters.events.ModEvents;
import yelf42.cropcritters.items.ModItems;
import yelf42.cropcritters.particle.ModParticles;

public class CropCritters implements ModInitializer {
	public static final String MOD_ID = "cropcritters";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final TagKey<EntityType<?>> WEED_IMMUNE = TagKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of("cropcritters", "weed_immune"));
	public static final TagKey<EntityType<?>> CROP_CRITTERS = TagKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of("cropcritters", "crop_critters"));
	public static final TagKey<EntityType<?>> SCARE_CRITTERS = TagKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of("cropcritters", "scare_critters"));
	public static final TagKey<EntityType<?>> HAS_LOST_SOUL = TagKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of("cropcritters", "has_lost_soul"));
	public static final TagKey<Block> UNDERWATER_STRANGE_FERTILIZERS = TagKey.of(RegistryKeys.BLOCK, Identifier.of("cropcritters", "underwater_strange_fertilizers"));
	public static final TagKey<Block> ON_LAND_COMMON_STRANGE_FERTILIZERS = TagKey.of(RegistryKeys.BLOCK, Identifier.of("cropcritters", "on_land_common_strange_fertilizers"));
	public static final TagKey<Block> ON_LAND_RARE_STRANGE_FERTILIZERS = TagKey.of(RegistryKeys.BLOCK, Identifier.of("cropcritters", "on_land_rare_strange_fertilizers"));
	public static final TagKey<Block> ON_NYLIUM_STRANGE_FERTILIZERS = TagKey.of(RegistryKeys.BLOCK, Identifier.of("cropcritters", "on_nylium_strange_fertilizers"));
	public static final TagKey<Block> IGNORE_STRANGE_FERTILIZERS = TagKey.of(RegistryKeys.BLOCK, Identifier.of("cropcritters", "ignore_strange_fertilizers"));
	public static final TagKey<Block> WEEDS = TagKey.of(RegistryKeys.BLOCK, Identifier.of("cropcritters", "weeds"));
	public static final TagKey<Block> SEED_BALL_CROPS = TagKey.of(RegistryKeys.BLOCK, Identifier.of("cropcritters", "seed_ball_crops"));
	public static final TagKey<Block> CAN_GROW_WEEDS = TagKey.of(RegistryKeys.BLOCK, Identifier.of("cropcritters", "can_grow_weeds"));


	@Override
	public void onInitialize() {
		LOGGER.info("Starting initialize of " + CropCritters.MOD_ID);

		// Config load:
		ConfigManager.load();

		ModEntities.initialize();
		ModItems.initialize();
		ModBlocks.initialize();
		ModEvents.initialize();
		ModParticles.initialize();

		// Vanilla biome mods
		LOGGER.info("Starting biome changes for " + CropCritters.MOD_ID);
		if (ConfigManager.CONFIG.deadCoralGeneration) {
			BiomeModifications.addFeature(
					BiomeSelectors.tag(BiomeTags.IS_BEACH),
					GenerationStep.Feature.UNDERGROUND_ORES,
					RegistryKey.of(RegistryKeys.PLACED_FEATURE, Identifier.of("cropcritters", "dead_coral_shelf"))
			);
		}
		if (ConfigManager.CONFIG.thornweedGeneration) {
			BiomeModifications.addFeature(
					BiomeSelectors.includeByKey(BiomeKeys.CRIMSON_FOREST),
					GenerationStep.Feature.VEGETAL_DECORATION,
					RegistryKey.of(RegistryKeys.PLACED_FEATURE, Identifier.of("cropcritters", "crimson_thornweed"))
			);
		}
		if (ConfigManager.CONFIG.waftgrassGeneration) {
			BiomeModifications.addFeature(
					BiomeSelectors.includeByKey(BiomeKeys.WARPED_FOREST),
					GenerationStep.Feature.VEGETAL_DECORATION,
					RegistryKey.of(RegistryKeys.PLACED_FEATURE, Identifier.of("cropcritters", "waftgrass"))
			);
		}
		if (ConfigManager.CONFIG.spiteweedGeneration) {
			BiomeModifications.addFeature(
					BiomeSelectors.includeByKey(BiomeKeys.SOUL_SAND_VALLEY),
					GenerationStep.Feature.SURFACE_STRUCTURES,
					RegistryKey.of(RegistryKeys.PLACED_FEATURE, Identifier.of("cropcritters", "withering_spiteweed"))
			);
		}


		// S2C Packets
		PayloadTypeRegistry.playS2C().register(WaterSprayS2CPayload.ID, WaterSprayS2CPayload.CODEC);

	}

	// Custom packet payloads
	public record WaterSprayS2CPayload(Vec3d pos, Vec3d dir) implements CustomPayload {
		public static final Identifier WATER_SPRAY_PAYLOAD_ID = Identifier.of(CropCritters.MOD_ID, "water_spray_packet");
		public static final CustomPayload.Id<WaterSprayS2CPayload> ID = new CustomPayload.Id<>(WATER_SPRAY_PAYLOAD_ID);
		public static final PacketCodec<RegistryByteBuf, WaterSprayS2CPayload> CODEC = PacketCodec.tuple(Vec3d.PACKET_CODEC, WaterSprayS2CPayload::pos, Vec3d.PACKET_CODEC, WaterSprayS2CPayload::dir, WaterSprayS2CPayload::new);

		@Override
		public Id<? extends CustomPayload> getId() {
			return ID;
		}
	}
}