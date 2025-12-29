package yelf42.cropcritters.renderer.blockentity;

import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.jspecify.annotations.Nullable;
import yelf42.cropcritters.blocks.StrangleFernBlockEntity;


public class StrangleFernBlockEntityRenderer implements BlockEntityRenderer<StrangleFernBlockEntity, StrangleFernBlockEntityRenderState> {

    public StrangleFernBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {}

    @Override
    public StrangleFernBlockEntityRenderState createRenderState() {
        return new StrangleFernBlockEntityRenderState();
    }

    @Override
    public void updateRenderState(StrangleFernBlockEntity blockEntity, StrangleFernBlockEntityRenderState state, float tickProgress, Vec3d cameraPos, ModelCommandRenderer.@Nullable CrumblingOverlayCommand crumblingOverlay) {
        BlockEntityRenderer.super.updateRenderState(blockEntity, state, tickProgress, cameraPos, crumblingOverlay);
        state.infestedBlock = blockEntity.getInfestedState();
    }

    @Override
    public void render(StrangleFernBlockEntityRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        if (state.infestedBlock == null || state.infestedBlock.isOf(Blocks.AIR)) return;
        //queue.submitBlock(matrices, state.infestedBlock, state.lightmapCoordinates, OverlayTexture.DEFAULT_UV, 0);

        MinecraftClient client = MinecraftClient.getInstance();

        int tint = client.getBlockColors().getColor(state.infestedBlock, client.world, state.pos, 0);
        float r = (float)((tint >> 16) & 0xFF) / 255f;
        float g = (float)((tint >> 8) & 0xFF) / 255f;
        float b = (float)(tint & 0xFF) / 255f;

        BlockRenderManager blockRenderManager = client.getBlockRenderManager();
        BlockStateModel model = blockRenderManager.getModel(state.infestedBlock);

        BlockModelRenderer.render(
                matrices.peek(),
                client.getBufferBuilders().getEntityVertexConsumers().getBuffer(RenderLayers.cutout()),
                model,
                r, g, b,
                state.lightmapCoordinates,
                OverlayTexture.DEFAULT_UV
        );
    }
}
