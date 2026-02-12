package yelf42.cropcritters;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.resources.Identifier;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.GenerationStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yelf42.cropcritters.area_affectors.AffectorPositions;
import yelf42.cropcritters.blocks.ModBlockEntities;
import yelf42.cropcritters.blocks.ModBlocks;
import yelf42.cropcritters.config.ConfigManager;
import yelf42.cropcritters.effects.ModEffects;
import yelf42.cropcritters.entity.ModEntities;
import yelf42.cropcritters.features.ModFeatures;
import yelf42.cropcritters.items.ModComponents;
import yelf42.cropcritters.items.ModItems;
import yelf42.cropcritters.items.StrangeFertilizerItem;
import yelf42.cropcritters.particle.ModParticles;
import yelf42.cropcritters.sound.ModSounds;

public class CropCritters implements ModInitializer {
	public static final String MOD_ID = "cropcritters";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final String[] INT_TO_ROMAN = {" ", " I", " II", " III", " IV", " V", " VI", " VII", " VIII", " IX", " X"};

	public static final TagKey<EntityType<?>> WEED_IMMUNE = TagKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MOD_ID, "weed_immune"));
	public static final TagKey<EntityType<?>> CROP_CRITTERS = TagKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MOD_ID, "crop_critters"));
	public static final TagKey<EntityType<?>> SCARE_CRITTERS = TagKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MOD_ID, "scare_critters"));
	public static final TagKey<EntityType<?>> HAS_LOST_SOUL = TagKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MOD_ID, "has_lost_soul"));

    public static final TagKey<Block> UNDERWATER_STRANGE_FERTILIZERS = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(MOD_ID, "underwater_strange_fertilizers"));
	public static final TagKey<Block> ON_LAND_COMMON_STRANGE_FERTILIZERS = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(MOD_ID, "on_land_common_strange_fertilizers"));
	public static final TagKey<Block> ON_LAND_RARE_STRANGE_FERTILIZERS = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(MOD_ID, "on_land_rare_strange_fertilizers"));
	public static final TagKey<Block> ON_NYLIUM_STRANGE_FERTILIZERS = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(MOD_ID, "on_nylium_strange_fertilizers"));
    public static final TagKey<Block> ON_MYCELIUM_STRANGE_FERTILIZERS = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(MOD_ID, "on_mycelium_strange_fertilizers"));
	public static final TagKey<Block> IGNORE_STRANGE_FERTILIZERS = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(MOD_ID, "ignore_strange_fertilizers"));
	public static final TagKey<Block> WEEDS = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(MOD_ID, "weeds"));
    public static final TagKey<Block> PATH_PENALTY_WEEDS = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(MOD_ID, "path_penalty_weeds"));
    public static final TagKey<Block> SPORES_INFECT = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(MOD_ID, "spores_infectable"));
    public static final TagKey<Block> IMMUNE_PLANTS = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(MOD_ID, "immune_plants"));
    public static final TagKey<Block> SNOW_FALL_KILLS = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(MOD_ID, "snow_fall_kills"));


    public static final TagKey<Item> SEED_BALL_CROPS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "seed_ball_crops"));


    public static final AttachmentType<AffectorPositions> AFFECTOR_POSITIONS_ATTACHMENT_TYPE =
            AttachmentRegistry.<AffectorPositions>builder()
                    .persistent(AffectorPositions.CODEC)
                    .buildAndRegister(Identifier.fromNamespaceAndPath(MOD_ID, "affector_positions"));

	@Override
	public void onInitialize() {
		LOGGER.info("Starting initialize of " + CropCritters.MOD_ID);

		// Config load:
        ConfigManager.setConfigPath(FabricLoader.getInstance().getConfigDir());
		ConfigManager.load();

        ModParticles.initialize();
        ModSounds.initialize();
        ModComponents.initialize();
        ModEffects.initialize();
        ModFeatures.initialize();
		ModEntities.initialize();
		ModItems.initialize();
		ModBlocks.initialize();
		ModBlockEntities.initialize();

		// Strange fertilizer dispenser behaviour
		LOGGER.info("Registering dispenser behaviours for " + CropCritters.MOD_ID);
		DispenserBlock.registerBehavior(ModItems.STRANGE_FERTILIZER, new OptionalDispenseItemBehavior() {
			protected ItemStack execute(BlockSource pointer, ItemStack stack) {
				this.setSuccess(true);
				Level world = pointer.level();
				Direction facing = pointer.state().getValue(DispenserBlock.FACING);
				BlockPos blockPos = pointer.pos().relative(facing);
				BlockState state = world.getBlockState(blockPos);
				if (!StrangeFertilizerItem.tryReviveCoral(stack, world, blockPos, state)
						&& !StrangeFertilizerItem.useOnBush(stack, world, blockPos)
						&& !StrangeFertilizerItem.growCrop(stack, world, blockPos)
						&& !StrangeFertilizerItem.useOnGround(stack, world, blockPos, blockPos, facing)) {
					this.setSuccess(false);
				} else if (!world.isClientSide()) {
					world.levelEvent(1505, blockPos, 15);
				}
				return stack;
			}
		});

		// Vanilla biome mods
		LOGGER.info("Starting biome changes for " + CropCritters.MOD_ID);
		if (ConfigManager.CONFIG.deadCoralGeneration) {
			BiomeModifications.addFeature(
					BiomeSelectors.tag(BiomeTags.IS_BEACH),
					GenerationStep.Decoration.UNDERGROUND_ORES,
					ResourceKey.create(Registries.PLACED_FEATURE, Identifier.fromNamespaceAndPath(CropCritters.MOD_ID, "dead_coral_shelf"))
			);
		}
		if (ConfigManager.CONFIG.thornweedGeneration) {
			BiomeModifications.addFeature(
					BiomeSelectors.includeByKey(Biomes.CRIMSON_FOREST),
					GenerationStep.Decoration.VEGETAL_DECORATION,
					ResourceKey.create(Registries.PLACED_FEATURE, Identifier.fromNamespaceAndPath(CropCritters.MOD_ID, "crimson_thornweed"))
			);
		}
		if (ConfigManager.CONFIG.waftgrassGeneration) {
			BiomeModifications.addFeature(
					BiomeSelectors.includeByKey(Biomes.WARPED_FOREST),
					GenerationStep.Decoration.VEGETAL_DECORATION,
					ResourceKey.create(Registries.PLACED_FEATURE, Identifier.fromNamespaceAndPath(CropCritters.MOD_ID, "waftgrass"))
			);
		}
		if (ConfigManager.CONFIG.spiteweedGeneration) {
			BiomeModifications.addFeature(
					BiomeSelectors.includeByKey(Biomes.SOUL_SAND_VALLEY),
					GenerationStep.Decoration.SURFACE_STRUCTURES,
					ResourceKey.create(Registries.PLACED_FEATURE, Identifier.fromNamespaceAndPath(CropCritters.MOD_ID, "withering_spiteweed"))
			);
		}
        if (ConfigManager.CONFIG.strangleFernGeneration) {
            BiomeModifications.addFeature(
                    BiomeSelectors.includeByKey(Biomes.SWAMP),
                    GenerationStep.Decoration.VEGETAL_DECORATION,
                    ResourceKey.create(Registries.PLACED_FEATURE, Identifier.fromNamespaceAndPath(CropCritters.MOD_ID, "strangle_fern"))
            );
        }
        if (ConfigManager.CONFIG.liverwortGeneration) {
            BiomeModifications.addFeature(
                    BiomeSelectors.includeByKey(Biomes.SWAMP),
                    GenerationStep.Decoration.VEGETAL_DECORATION,
                    ResourceKey.create(Registries.PLACED_FEATURE, Identifier.fromNamespaceAndPath(CropCritters.MOD_ID, "liverwort"))
            );
        }
        if (ConfigManager.CONFIG.puffbombGeneration) {
            BiomeModifications.addFeature(
                    BiomeSelectors.includeByKey(Biomes.PLAINS),
                    GenerationStep.Decoration.VEGETAL_DECORATION,
                    ResourceKey.create(Registries.PLACED_FEATURE, Identifier.fromNamespaceAndPath(CropCritters.MOD_ID, "plains_puffbomb_blob"))
            );
        }
        if (ConfigManager.CONFIG.soulRoseHintGeneration) {
            BiomeModifications.addFeature(
                    BiomeSelectors.includeByKey(Biomes.SOUL_SAND_VALLEY),
                    GenerationStep.Decoration.VEGETAL_DECORATION,
                    ResourceKey.create(Registries.PLACED_FEATURE, Identifier.fromNamespaceAndPath(CropCritters.MOD_ID, "soul_rose_hint"))
            );
        }


		// S2C Packets
		PayloadTypeRegistry.playS2C().register(WaterSprayS2CPayload.ID, WaterSprayS2CPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ParticleRingS2CPayload.ID, ParticleRingS2CPayload.CODEC);

	}

	// Custom packet payloads
	public record WaterSprayS2CPayload(Vec3 pos, Vec3 dir) implements CustomPacketPayload {
		public static final Identifier WATER_SPRAY_PAYLOAD_ID = Identifier.fromNamespaceAndPath(CropCritters.MOD_ID, "water_spray_packet");
		public static final CustomPacketPayload.Type<WaterSprayS2CPayload> ID = new CustomPacketPayload.Type<>(WATER_SPRAY_PAYLOAD_ID);
		public static final StreamCodec<RegistryFriendlyByteBuf, WaterSprayS2CPayload> CODEC = StreamCodec.composite(
                Vec3.STREAM_CODEC, WaterSprayS2CPayload::pos,
                Vec3.STREAM_CODEC, WaterSprayS2CPayload::dir,
                WaterSprayS2CPayload::new
        );

		@Override
		public Type<? extends CustomPacketPayload> type() {
			return ID;
		}
	}

    public record ParticleRingS2CPayload(Vec3 pos, float radius, int count, ParticleOptions effect) implements CustomPacketPayload {
        public static final Identifier PARTICLE_RING_PAYLOAD_ID = Identifier.fromNamespaceAndPath(CropCritters.MOD_ID, "particle_ring_packet");
        public static final CustomPacketPayload.Type<ParticleRingS2CPayload> ID = new CustomPacketPayload.Type<>(PARTICLE_RING_PAYLOAD_ID);
        public static final StreamCodec<RegistryFriendlyByteBuf, ParticleRingS2CPayload> CODEC = StreamCodec.composite(
                Vec3.STREAM_CODEC, ParticleRingS2CPayload::pos,
                ByteBufCodecs.FLOAT, ParticleRingS2CPayload::radius,
                ByteBufCodecs.INT, ParticleRingS2CPayload::count,
                ParticleTypes.STREAM_CODEC, ParticleRingS2CPayload::effect,
                ParticleRingS2CPayload::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }
}