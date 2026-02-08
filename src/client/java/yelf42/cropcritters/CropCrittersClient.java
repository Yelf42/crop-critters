package yelf42.cropcritters;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.render.entity.EntityRendererFactories;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import net.minecraft.client.render.item.model.special.SpecialModelTypes;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import yelf42.cropcritters.blocks.ModBlockEntities;
import yelf42.cropcritters.blocks.ModBlocks;
import yelf42.cropcritters.entity.ModEntities;
import yelf42.cropcritters.particle.ModParticles;
import yelf42.cropcritters.particle.SoulSiphonParticle;
import yelf42.cropcritters.particle.SporeParticle;
import yelf42.cropcritters.particle.WaterSprayParticle;
import yelf42.cropcritters.renderer.blockentity.SoulPotBlockEntityRenderer;
import yelf42.cropcritters.renderer.blockentity.SoulPotModelRenderer;
import yelf42.cropcritters.renderer.blockentity.StrangleFernBlockEntityRenderer;
import yelf42.cropcritters.renderer.entity.AbstractCritterRenderer;
import yelf42.cropcritters.renderer.entity.PopperPodEntityRenderer;

public class CropCrittersClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// To make some parts of the block transparent (like glass, saplings and doors):
		BlockRenderLayerMap.putBlock(ModBlocks.LOST_SOUL_IN_A_JAR, BlockRenderLayer.TRANSLUCENT);
		BlockRenderLayerMap.putBlock(ModBlocks.CRAWL_THISTLE, BlockRenderLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(ModBlocks.CRIMSON_THORNWEED, BlockRenderLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(ModBlocks.WAFTGRASS, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.PUFFBOMB_MUSHROOM, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.LIVERWORT, BlockRenderLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(ModBlocks.WITHERING_SPITEWEED, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.POPPER_PLANT, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.BONE_TRAP, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.SOUL_ROSE, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.TRIMMED_SOUL_ROSE, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.POTTED_SOUL_ROSE, BlockRenderLayer.CUTOUT);

        BlockRenderLayerMap.putBlock(ModBlocks.TALL_BUSH, BlockRenderLayer.CUTOUT);
        ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) ->
				world != null && pos != null
                ? BiomeColors.getGrassColor(world, pos)
                : 0x91BD59, ModBlocks.TALL_BUSH
		);
		BlockRenderLayerMap.putBlock(ModBlocks.ORNAMENTAL_BUSH, BlockRenderLayer.CUTOUT);
		ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) ->
				world != null && pos != null
						? BiomeColors.getGrassColor(world, pos)
						: 0x91BD59, ModBlocks.ORNAMENTAL_BUSH
		);
        BlockRenderLayerMap.putBlock(ModBlocks.MAZEWOOD, BlockRenderLayer.CUTOUT);
        ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) ->
                world != null && pos != null
                        ? BiomeColors.getGrassColor(world, pos)
                        : 0x91BD59, ModBlocks.MAZEWOOD
        );
        BlockRenderLayerMap.putBlock(ModBlocks.MAZEWOOD_SAPLING, BlockRenderLayer.CUTOUT);
        ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) ->
                world != null && pos != null
                        ? BiomeColors.getGrassColor(world, pos)
                        : 0x91BD59, ModBlocks.MAZEWOOD_SAPLING
        );

        BlockRenderLayerMap.putBlock(ModBlocks.STRANGLE_FERN, BlockRenderLayer.CUTOUT);
        BlockEntityRendererFactories.register(ModBlockEntities.STRANGLE_FERN, StrangleFernBlockEntityRenderer::new);
        ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) ->
                world != null && pos != null
                        ? BiomeColors.getGrassColor(world, pos)
                        : 0x91BD59, ModBlocks.STRANGLE_FERN
        );

        SpecialModelTypes.ID_MAPPER.put(
                Identifier.of(CropCritters.MOD_ID, "soul_pot"),
                SoulPotModelRenderer.Unbaked.CODEC
        );
        BlockEntityRendererFactories.register(ModBlockEntities.SOUL_POT, SoulPotBlockEntityRenderer::new);

        // Entities
        EntityRendererFactories.register(ModEntities.WHEAT_CRITTER, context -> new AbstractCritterRenderer<>(context, Identifier.of(CropCritters.MOD_ID, "wheat_critter"), true));
        EntityRendererFactories.register(ModEntities.MELON_CRITTER, context -> new AbstractCritterRenderer<>(context, Identifier.of(CropCritters.MOD_ID, "melon_critter"), true));
        EntityRendererFactories.register(ModEntities.PUMPKIN_CRITTER, context -> new AbstractCritterRenderer<>(context, Identifier.of(CropCritters.MOD_ID, "pumpkin_critter"), false));
        EntityRendererFactories.register(ModEntities.POTATO_CRITTER, context -> new AbstractCritterRenderer<>(context, Identifier.of(CropCritters.MOD_ID, "potato_critter"), true));
        EntityRendererFactories.register(ModEntities.CARROT_CRITTER, context -> new AbstractCritterRenderer<>(context, Identifier.of(CropCritters.MOD_ID, "carrot_critter"), true));
        EntityRendererFactories.register(ModEntities.BEETROOT_CRITTER, context -> new AbstractCritterRenderer<>(context, Identifier.of(CropCritters.MOD_ID, "beetroot_critter"), true));
        EntityRendererFactories.register(ModEntities.NETHER_WART_CRITTER, context -> new AbstractCritterRenderer<>(context, Identifier.of(CropCritters.MOD_ID, "nether_wart_critter"), true));
        EntityRendererFactories.register(ModEntities.POISONOUS_POTATO_CRITTER, context -> new AbstractCritterRenderer<>(context, Identifier.of(CropCritters.MOD_ID, "poisonous_potato_critter"), true));
        EntityRendererFactories.register(ModEntities.TORCHFLOWER_CRITTER, context -> new AbstractCritterRenderer<>(context, Identifier.of(CropCritters.MOD_ID, "torchflower_critter"), true));
        EntityRendererFactories.register(ModEntities.PITCHER_CRITTER, context -> new AbstractCritterRenderer<>(context, Identifier.of(CropCritters.MOD_ID, "pitcher_critter"), false));
        EntityRendererFactories.register(ModEntities.COCOA_CRITTER, context -> new AbstractCritterRenderer<>(context, Identifier.of(CropCritters.MOD_ID, "cocoa_critter"), false));

        EntityRendererFactories.register(ModEntities.SEED_BALL_PROJECTILE, FlyingItemEntityRenderer::new);
        EntityRendererFactories.register(ModEntities.SPIT_SEED_PROJECTILE, FlyingItemEntityRenderer::new);
        EntityRendererFactories.register(ModEntities.POPPER_POD_PROJECTILE, PopperPodEntityRenderer::new);
        EntityRendererFactories.register(ModEntities.POPPER_SEED_PROJECTILE, FlyingItemEntityRenderer::new);
        EntityRendererFactories.register(ModEntities.HERBICIDE_PROJECTILE, FlyingItemEntityRenderer::new);

		// Particles
		ParticleFactoryRegistry.getInstance().register(ModParticles.WATER_SPRAY_PARTICLE, WaterSprayParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(ModParticles.SPORE_PARTICLE, SporeParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(ModParticles.SOUL_SIPHON_PARTICLE, SoulSiphonParticle.Factory::new);

		// Packet Handling
		ClientPlayNetworking.registerGlobalReceiver(CropCritters.WaterSprayS2CPayload.ID, (payload, context) -> {
			ClientWorld world = context.client().world;

			if (world == null) {
				return;
			}

			Vec3d pos = payload.pos();
			Vec3d dir = payload.dir();
			world.addParticleClient(ModParticles.WATER_SPRAY_PARTICLE, pos.x, pos.y + 0.2, pos.z, dir.x, 0, dir.z);
		});
	}
}