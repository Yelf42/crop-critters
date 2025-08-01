package yelf42.cropcritters;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import yelf42.cropcritters.blocks.ModBlocks;
import yelf42.cropcritters.entity.ModEntities;
import yelf42.cropcritters.particle.ModParticles;
import yelf42.cropcritters.particle.WaterSprayParticle;
import yelf42.cropcritters.renderer.entity.AbstractCritterRenderer;

public class CropCrittersClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// To make some parts of the block transparent (like glass, saplings and doors):
		BlockRenderLayerMap.putBlock(ModBlocks.LOST_SOUL_IN_A_JAR, BlockRenderLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(ModBlocks.CRAWL_THISTLE, BlockRenderLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(ModBlocks.CRIMSON_THORNWEED, BlockRenderLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(ModBlocks.WAFTGRASS, BlockRenderLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(ModBlocks.WITHERING_SPITEWEED, BlockRenderLayer.CUTOUT);
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

		// Entities
		EntityRendererRegistry.register(ModEntities.WHEAT_CRITTER, context -> new AbstractCritterRenderer<>(context, Identifier.of(CropCritters.MOD_ID, "wheat_critter"), true));
		EntityRendererRegistry.register(ModEntities.MELON_CRITTER, context -> new AbstractCritterRenderer<>(context, Identifier.of(CropCritters.MOD_ID, "melon_critter"), true));
		EntityRendererRegistry.register(ModEntities.PUMPKIN_CRITTER, context -> new AbstractCritterRenderer<>(context, Identifier.of(CropCritters.MOD_ID, "pumpkin_critter"), true));
		EntityRendererRegistry.register(ModEntities.POTATO_CRITTER, context -> new AbstractCritterRenderer<>(context, Identifier.of(CropCritters.MOD_ID, "potato_critter"), true));
		EntityRendererRegistry.register(ModEntities.CARROT_CRITTER, context -> new AbstractCritterRenderer<>(context, Identifier.of(CropCritters.MOD_ID, "carrot_critter"), true));
		EntityRendererRegistry.register(ModEntities.BEETROOT_CRITTER, context -> new AbstractCritterRenderer<>(context, Identifier.of(CropCritters.MOD_ID, "beetroot_critter"), true));
		EntityRendererRegistry.register(ModEntities.NETHER_WART_CRITTER, context -> new AbstractCritterRenderer<>(context, Identifier.of(CropCritters.MOD_ID, "nether_wart_critter"), true));
		EntityRendererRegistry.register(ModEntities.POISONOUS_POTATO_CRITTER, context -> new AbstractCritterRenderer<>(context, Identifier.of(CropCritters.MOD_ID, "poisonous_potato_critter"), true));
		EntityRendererRegistry.register(ModEntities.TORCHFLOWER_CRITTER, context -> new AbstractCritterRenderer<>(context, Identifier.of(CropCritters.MOD_ID, "torchflower_critter"), true));
		EntityRendererRegistry.register(ModEntities.PITCHER_CRITTER, context -> new AbstractCritterRenderer<>(context, Identifier.of(CropCritters.MOD_ID, "pitcher_critter"), true));

		EntityRendererRegistry.register(ModEntities.SEED_BALL_PROJECTILE, FlyingItemEntityRenderer::new);
		EntityRendererRegistry.register(ModEntities.SPIT_SEED_PROJECTILE, FlyingItemEntityRenderer::new);

		// Particles
		ParticleFactoryRegistry.getInstance().register(ModParticles.WATER_SPRAY_PARTICLE, WaterSprayParticle.Factory::new);

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