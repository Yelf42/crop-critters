package yelf42.cropcritters;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.render.BlockRenderLayer;
import yelf42.cropcritters.blocks.ModBlocks;

public class CropCrittersClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// To make some parts of the block transparent (like glass, saplings and doors):
		BlockRenderLayerMap.putBlock(ModBlocks.CRAWL_THISTLE, BlockRenderLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(ModBlocks.CRIMSON_THORNWEED, BlockRenderLayer.CUTOUT);
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

		// To make some parts of the block translucent (like ice, stained glass and portal)
		//BlockRenderLayerMap.putBlock(TutorialBlocks.MY_BLOCK, BlockRenderLayer.TRANSLUCENT);
	}
}