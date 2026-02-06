package yelf42.cropcritters;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.FallibleItemDispenserBehavior;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.gen.GenerationStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yelf42.cropcritters.area_affectors.AffectorPositions;
import yelf42.cropcritters.blocks.ModBlockEntities;
import yelf42.cropcritters.blocks.ModBlocks;
import yelf42.cropcritters.config.ConfigManager;
import yelf42.cropcritters.effects.ModEffects;
import yelf42.cropcritters.entity.ModEntities;
import yelf42.cropcritters.events.ModEvents;
import yelf42.cropcritters.features.ModFeatures;
import yelf42.cropcritters.items.ModComponents;
import yelf42.cropcritters.items.ModItems;
import yelf42.cropcritters.items.StrangeFertilizerItem;
import yelf42.cropcritters.particle.ModParticles;

// TODO soul rose effects (iron)
// TODO soul rose hint structure
public class CropCritters implements ModInitializer {
	public static final String MOD_ID = "cropcritters";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final TagKey<EntityType<?>> WEED_IMMUNE = TagKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(MOD_ID, "weed_immune"));
	public static final TagKey<EntityType<?>> CROP_CRITTERS = TagKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(MOD_ID, "crop_critters"));
	public static final TagKey<EntityType<?>> SCARE_CRITTERS = TagKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(MOD_ID, "scare_critters"));
	public static final TagKey<EntityType<?>> HAS_LOST_SOUL = TagKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(MOD_ID, "has_lost_soul"));

    public static final TagKey<Block> UNDERWATER_STRANGE_FERTILIZERS = TagKey.of(RegistryKeys.BLOCK, Identifier.of(MOD_ID, "underwater_strange_fertilizers"));
	public static final TagKey<Block> ON_LAND_COMMON_STRANGE_FERTILIZERS = TagKey.of(RegistryKeys.BLOCK, Identifier.of(MOD_ID, "on_land_common_strange_fertilizers"));
	public static final TagKey<Block> ON_LAND_RARE_STRANGE_FERTILIZERS = TagKey.of(RegistryKeys.BLOCK, Identifier.of(MOD_ID, "on_land_rare_strange_fertilizers"));
	public static final TagKey<Block> ON_NYLIUM_STRANGE_FERTILIZERS = TagKey.of(RegistryKeys.BLOCK, Identifier.of(MOD_ID, "on_nylium_strange_fertilizers"));
    public static final TagKey<Block> ON_MYCELIUM_STRANGE_FERTILIZERS = TagKey.of(RegistryKeys.BLOCK, Identifier.of(MOD_ID, "on_mycelium_strange_fertilizers"));
	public static final TagKey<Block> IGNORE_STRANGE_FERTILIZERS = TagKey.of(RegistryKeys.BLOCK, Identifier.of(MOD_ID, "ignore_strange_fertilizers"));
	public static final TagKey<Block> WEEDS = TagKey.of(RegistryKeys.BLOCK, Identifier.of(MOD_ID, "weeds"));
    public static final TagKey<Block> PATH_PENALTY_WEEDS = TagKey.of(RegistryKeys.BLOCK, Identifier.of(MOD_ID, "path_penalty_weeds"));
    public static final TagKey<Block> SPORES_INFECT = TagKey.of(RegistryKeys.BLOCK, Identifier.of(MOD_ID, "spores_infectable"));
    public static final TagKey<Block> IMMUNE_PLANTS = TagKey.of(RegistryKeys.BLOCK, Identifier.of(MOD_ID, "immune_plants"));
    public static final TagKey<Block> SNOW_FALL_KILLS = TagKey.of(RegistryKeys.BLOCK, Identifier.of(MOD_ID, "snow_fall_kills"));


    public static final TagKey<Item> SEED_BALL_CROPS = TagKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "seed_ball_crops"));


    public static final AttachmentType<AffectorPositions> AFFECTOR_POSITIONS_ATTACHMENT_TYPE =
            AttachmentRegistry.<AffectorPositions>builder()
                    .persistent(AffectorPositions.CODEC)
                    .buildAndRegister(Identifier.of(MOD_ID, "affector_positions"));

	@Override
	public void onInitialize() {
		LOGGER.info("Starting initialize of " + CropCritters.MOD_ID);

		// Config load:
		ConfigManager.load();

		ModEntities.initialize();
		ModItems.initialize();
		ModBlocks.initialize();
        ModComponents.initialize();
		ModBlockEntities.initialize();
		ModEvents.initialize();
		ModParticles.initialize();
        ModEffects.initialize();
        ModFeatures.initialize();

		// Strange fertilizer dispenser behaviour
		LOGGER.info("Registering dispenser behaviours for " + CropCritters.MOD_ID);
		DispenserBlock.registerBehavior(ModItems.STRANGE_FERTILIZER, new FallibleItemDispenserBehavior() {
			protected ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
				this.setSuccess(true);
				World world = pointer.world();
				Direction facing = (Direction)pointer.state().get(DispenserBlock.FACING);
				BlockPos blockPos = pointer.pos().offset(facing);
				BlockState state = world.getBlockState(blockPos);
				if (!StrangeFertilizerItem.tryReviveCoral(stack, world, blockPos, state)
						&& !StrangeFertilizerItem.useOnBush(stack, world, blockPos)
						&& !StrangeFertilizerItem.useOnFertilizable(stack, world, blockPos)
						&& !StrangeFertilizerItem.useOnGround(stack, world, blockPos, blockPos, facing)) {
					this.setSuccess(false);
				} else if (!world.isClient()) {
					world.syncWorldEvent(1505, blockPos, 15);
				}
				return stack;
			}
		});

		// Vanilla biome mods
		LOGGER.info("Starting biome changes for " + CropCritters.MOD_ID);
		if (ConfigManager.CONFIG.deadCoralGeneration) {
			BiomeModifications.addFeature(
					BiomeSelectors.tag(BiomeTags.IS_BEACH),
					GenerationStep.Feature.UNDERGROUND_ORES,
					RegistryKey.of(RegistryKeys.PLACED_FEATURE, Identifier.of(CropCritters.MOD_ID, "dead_coral_shelf"))
			);
		}
		if (ConfigManager.CONFIG.thornweedGeneration) {
			BiomeModifications.addFeature(
					BiomeSelectors.includeByKey(BiomeKeys.CRIMSON_FOREST),
					GenerationStep.Feature.VEGETAL_DECORATION,
					RegistryKey.of(RegistryKeys.PLACED_FEATURE, Identifier.of(CropCritters.MOD_ID, "crimson_thornweed"))
			);
		}
		if (ConfigManager.CONFIG.waftgrassGeneration) {
			BiomeModifications.addFeature(
					BiomeSelectors.includeByKey(BiomeKeys.WARPED_FOREST),
					GenerationStep.Feature.VEGETAL_DECORATION,
					RegistryKey.of(RegistryKeys.PLACED_FEATURE, Identifier.of(CropCritters.MOD_ID, "waftgrass"))
			);
		}
		if (ConfigManager.CONFIG.spiteweedGeneration) {
			BiomeModifications.addFeature(
					BiomeSelectors.includeByKey(BiomeKeys.SOUL_SAND_VALLEY),
					GenerationStep.Feature.SURFACE_STRUCTURES,
					RegistryKey.of(RegistryKeys.PLACED_FEATURE, Identifier.of(CropCritters.MOD_ID, "withering_spiteweed"))
			);
		}
        if (ConfigManager.CONFIG.strangleFernGeneration) {
            BiomeModifications.addFeature(
                    BiomeSelectors.includeByKey(BiomeKeys.SWAMP),
                    GenerationStep.Feature.VEGETAL_DECORATION,
                    RegistryKey.of(RegistryKeys.PLACED_FEATURE, Identifier.of(CropCritters.MOD_ID, "strangle_fern"))
            );
        }
        if (ConfigManager.CONFIG.liverwortGeneration) {
            BiomeModifications.addFeature(
                    BiomeSelectors.includeByKey(BiomeKeys.SWAMP),
                    GenerationStep.Feature.VEGETAL_DECORATION,
                    RegistryKey.of(RegistryKeys.PLACED_FEATURE, Identifier.of(CropCritters.MOD_ID, "liverwort"))
            );
        }
        if (ConfigManager.CONFIG.puffbombGeneration) {
            BiomeModifications.addFeature(
                    BiomeSelectors.includeByKey(BiomeKeys.PLAINS),
                    GenerationStep.Feature.VEGETAL_DECORATION,
                    RegistryKey.of(RegistryKeys.PLACED_FEATURE, Identifier.of(CropCritters.MOD_ID, "plains_puffbomb_blob"))
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