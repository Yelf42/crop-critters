package yelf42.cropcritters.renderer.entity;

import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.state.FireworkRocketEntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.util.math.RotationAxis;
import yelf42.cropcritters.entity.PopperPodEntity;

public class PopperPodEntityRenderer extends EntityRenderer<PopperPodEntity, FireworkRocketEntityRenderState> {
    private final ItemModelManager itemModelManager;

    public PopperPodEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.itemModelManager = context.getItemModelManager();
    }

    public void render(FireworkRocketEntityRenderState fireworkRocketEntityRenderState, MatrixStack matrixStack, OrderedRenderCommandQueue orderedRenderCommandQueue, CameraRenderState cameraRenderState) {
        matrixStack.push();

        matrixStack.multiply(cameraRenderState.orientation);
        matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(45.0F));
        if (fireworkRocketEntityRenderState.shotAtAngle) {
            matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180.0F));
            matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F));
            matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0F));
        }


        fireworkRocketEntityRenderState.stack.render(matrixStack, orderedRenderCommandQueue, fireworkRocketEntityRenderState.light, OverlayTexture.DEFAULT_UV, fireworkRocketEntityRenderState.outlineColor);
        matrixStack.pop();
        super.render(fireworkRocketEntityRenderState, matrixStack, orderedRenderCommandQueue, cameraRenderState);
    }

    public FireworkRocketEntityRenderState createRenderState() {
        return new FireworkRocketEntityRenderState();
    }

    public void updateRenderState(PopperPodEntity popperPodEntity, FireworkRocketEntityRenderState fireworkRocketEntityRenderState, float f) {
        super.updateRenderState(popperPodEntity, fireworkRocketEntityRenderState, f);
        fireworkRocketEntityRenderState.shotAtAngle = popperPodEntity.wasShotAtAngle();
        this.itemModelManager.updateForNonLivingEntity(fireworkRocketEntityRenderState.stack, popperPodEntity.getStack(), ItemDisplayContext.GROUND, popperPodEntity);
    }
}