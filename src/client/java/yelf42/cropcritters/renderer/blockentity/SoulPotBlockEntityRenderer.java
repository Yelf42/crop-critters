package yelf42.cropcritters.renderer.blockentity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.DecoratedPotBlockEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.LoadedEntityModels;
import net.minecraft.client.render.item.model.special.SpecialModelRenderer;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.SpriteHolder;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;
import yelf42.cropcritters.CropCritters;
import yelf42.cropcritters.blocks.SoulPotBlockEntity;
import yelf42.cropcritters.items.ModItems;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class SoulPotBlockEntityRenderer implements BlockEntityRenderer<SoulPotBlockEntity, SoulPotBlockEntityRenderState> {
    private static final Map<Integer, SpriteIdentifier> COVER_SPRITES = new HashMap<>();
    private static final Map<Integer, SpriteIdentifier> INSIDE_SPRITES = new HashMap<>();


    private final SpriteHolder materials;
    private static final String NECK = "neck";
    private static final String FRONT = "front";
    private static final String BACK = "back";
    private static final String LEFT = "left";
    private static final String RIGHT = "right";
    private static final String TOP = "top";
    private static final String BOTTOM = "bottom";
    private final ModelPart neck;
    private final ModelPart front;
    private final ModelPart back;
    private final ModelPart left;
    private final ModelPart right;
    private final ModelPart top;
    private final ModelPart bottom;
    private static final float field_46728 = 0.125F;

    public SoulPotBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        this(context.loadedEntityModels(), context.spriteHolder());
    }

    public SoulPotBlockEntityRenderer(SpecialModelRenderer.BakeContext context) {
        this(context.entityModelSet(), context.spriteHolder());
    }

    public SoulPotBlockEntityRenderer(LoadedEntityModels entityModelSet, SpriteHolder materials) {
        this.materials = materials;
        ModelPart modelPart = entityModelSet.getModelPart(EntityModelLayers.DECORATED_POT_BASE);
        this.neck = modelPart.getChild("neck");
        this.top = modelPart.getChild("top");
        this.bottom = modelPart.getChild("bottom");
        ModelPart modelPart2 = entityModelSet.getModelPart(EntityModelLayers.DECORATED_POT_SIDES);
        this.front = modelPart2.getChild("front");
        this.back = modelPart2.getChild("back");
        this.left = modelPart2.getChild("left");
        this.right = modelPart2.getChild("right");
    }

    public static TexturedModelData getTopBottomNeckTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        Dilation dilation = new Dilation(0.2F);
        Dilation dilation2 = new Dilation(-0.1F);
        modelPartData.addChild("neck", ModelPartBuilder.create().uv(0, 0).cuboid(4.0F, 17.0F, 4.0F, 8.0F, 3.0F, 8.0F, dilation2).uv(0, 5).cuboid(5.0F, 20.0F, 5.0F, 6.0F, 1.0F, 6.0F, dilation), ModelTransform.of(0.0F, 37.0F, 16.0F, (float)Math.PI, 0.0F, 0.0F));
        ModelPartBuilder modelPartBuilder = ModelPartBuilder.create().uv(-14, 13).cuboid(0.0F, 0.0F, 0.0F, 14.0F, 0.0F, 14.0F);
        modelPartData.addChild("top", modelPartBuilder, ModelTransform.of(1.0F, 16.0F, 1.0F, 0.0F, 0.0F, 0.0F));
        modelPartData.addChild("bottom", modelPartBuilder, ModelTransform.of(1.0F, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F));
        return TexturedModelData.of(modelData, 32, 32);
    }

    public static TexturedModelData getSidesTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        ModelPartBuilder modelPartBuilder = ModelPartBuilder.create().uv(1, 0).cuboid(0.0F, 0.0F, 0.0F, 14.0F, 16.0F, 0.0F, EnumSet.of(Direction.NORTH));
        modelPartData.addChild("back", modelPartBuilder, ModelTransform.of(15.0F, 16.0F, 1.0F, 0.0F, 0.0F, (float)Math.PI));
        modelPartData.addChild("left", modelPartBuilder, ModelTransform.of(1.0F, 16.0F, 1.0F, 0.0F, (-(float)Math.PI / 2F), (float)Math.PI));
        modelPartData.addChild("right", modelPartBuilder, ModelTransform.of(15.0F, 16.0F, 15.0F, 0.0F, ((float)Math.PI / 2F), (float)Math.PI));
        modelPartData.addChild("front", modelPartBuilder, ModelTransform.of(1.0F, 16.0F, 15.0F, (float)Math.PI, 0.0F, 0.0F));
        return TexturedModelData.of(modelData, 16, 16);
    }

    public SoulPotBlockEntityRenderState createRenderState() {
        return new SoulPotBlockEntityRenderState();
    }

    public void updateRenderState(SoulPotBlockEntity soulPotBlockEntity, SoulPotBlockEntityRenderState soulPotBlockEntityRenderState, float f, Vec3d vec3d, ModelCommandRenderer.@Nullable CrumblingOverlayCommand crumblingOverlayCommand) {
        BlockEntityRenderer.super.updateRenderState(soulPotBlockEntity, soulPotBlockEntityRenderState, f, vec3d, crumblingOverlayCommand);
        soulPotBlockEntityRenderState.facing = soulPotBlockEntity.getHorizontalFacing();
        SoulPotBlockEntity.WobbleType wobbleType = soulPotBlockEntity.lastWobbleType;
        if (wobbleType != null && soulPotBlockEntity.getWorld() != null) {
            soulPotBlockEntityRenderState.wobbleAnimationProgress = ((float)(soulPotBlockEntity.getWorld().getTime() - soulPotBlockEntity.lastWobbleTime) + f) / (float)wobbleType.lengthInTicks;
        } else {
            soulPotBlockEntityRenderState.wobbleAnimationProgress = 0.0F;
        }
        soulPotBlockEntityRenderState.level = soulPotBlockEntity.getStack().isOf(ModItems.LOST_SOUL) ? Math.clamp(soulPotBlockEntity.getStack().getCount() / 2, 0, 12) : 0;
    }

    public void render(SoulPotBlockEntityRenderState soulPotBlockEntityRenderState, MatrixStack matrixStack, OrderedRenderCommandQueue orderedRenderCommandQueue, CameraRenderState cameraRenderState) {
        matrixStack.push();
        Direction direction = soulPotBlockEntityRenderState.facing;
        matrixStack.translate((double)0.5F, (double)0.0F, (double)0.5F);
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F - direction.getPositiveHorizontalDegrees()));
        matrixStack.translate((double)-0.5F, (double)0.0F, (double)-0.5F);
        if (soulPotBlockEntityRenderState.wobbleAnimationProgress >= 0.0F && soulPotBlockEntityRenderState.wobbleAnimationProgress <= 1.0F) {
            if (soulPotBlockEntityRenderState.wobbleType == DecoratedPotBlockEntity.WobbleType.POSITIVE) {
                float f = 0.015625F;
                float g = soulPotBlockEntityRenderState.wobbleAnimationProgress * ((float)Math.PI * 2F);
                float h = -1.5F * (MathHelper.cos((double)g) + 0.5F) * MathHelper.sin((double)(g / 2.0F));
                matrixStack.multiply(RotationAxis.POSITIVE_X.rotation(h * 0.015625F), 0.5F, 0.0F, 0.5F);
                float i = MathHelper.sin((double)g);
                matrixStack.multiply(RotationAxis.POSITIVE_Z.rotation(i * 0.015625F), 0.5F, 0.0F, 0.5F);
            } else {
                float f = MathHelper.sin((double)(-soulPotBlockEntityRenderState.wobbleAnimationProgress * 3.0F * (float)Math.PI)) * 0.125F;
                float g = 1.0F - soulPotBlockEntityRenderState.wobbleAnimationProgress;
                matrixStack.multiply(RotationAxis.POSITIVE_Y.rotation(f * g), 0.5F, 0.0F, 0.5F);
            }
        }

        this.render(matrixStack, orderedRenderCommandQueue, soulPotBlockEntityRenderState.lightmapCoordinates, OverlayTexture.DEFAULT_UV, 0, soulPotBlockEntityRenderState.level);
        matrixStack.pop();
    }

    public void render(MatrixStack matrices, OrderedRenderCommandQueue queue, int light, int overlay, int i, int level) {
        RenderLayer renderLayer = TexturedRenderLayers.DECORATED_POT_BASE.getRenderLayer(RenderLayers::entitySolid);
        Sprite sprite = this.materials.getSprite(TexturedRenderLayers.DECORATED_POT_BASE);
        queue.submitModelPart(this.neck, matrices, renderLayer, light, overlay, sprite, false, false, -1, null, i);
        queue.submitModelPart(this.top, matrices, renderLayer, light, overlay, sprite, false, false, -1, null, i);
        queue.submitModelPart(this.bottom, matrices, renderLayer, light, overlay, sprite, false, false, -1, null, i);

        SpriteIdentifier spriteIdentifier = COVER_SPRITES.get(level);
        Sprite stageSprite = this.materials.getSprite(spriteIdentifier);
        RenderLayer coverLayer = spriteIdentifier.getRenderLayer(RenderLayers::entityCutout);
        queue.submitModelPart(this.front, matrices, coverLayer, light, overlay, stageSprite, false, false, -1, null, i);
        queue.submitModelPart(this.back, matrices, coverLayer, light, overlay, stageSprite, false, false, -1, null, i);
        queue.submitModelPart(this.left, matrices, coverLayer, light, overlay, stageSprite, false, false, -1, null, i);
        queue.submitModelPart(this.right, matrices, coverLayer, light, overlay, stageSprite, false, false, -1, null, i);

        if (level > 0) {
            SpriteIdentifier spriteIdentifier2 = INSIDE_SPRITES.get(level);
            Sprite stageSprite2 = this.materials.getSprite(spriteIdentifier2);
            RenderLayer insideLayer = spriteIdentifier2.getRenderLayer(RenderLayers::entityCutout);
            queue.submitModelPart(this.front, matrices, insideLayer, 15728880, overlay, stageSprite2, false, false, -1, null, i);
            queue.submitModelPart(this.back, matrices, insideLayer, 15728880, overlay, stageSprite2, false, false, -1, null, i);
            queue.submitModelPart(this.left, matrices, insideLayer, 15728880, overlay, stageSprite2, false, false, -1, null, i);
            queue.submitModelPart(this.right, matrices, insideLayer, 15728880, overlay, stageSprite2, false, false, -1, null, i);
        }
    }

    public void collectVertices(Consumer<Vector3fc> consumer) {
        MatrixStack matrixStack = new MatrixStack();
        this.neck.collectVertices(matrixStack, consumer);
        this.top.collectVertices(matrixStack, consumer);
        this.bottom.collectVertices(matrixStack, consumer);
    }

    static {
        for (int i = 0; i < 13; i++) {
            COVER_SPRITES.put(i, new SpriteIdentifier(
                    SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE,
                    Identifier.of(CropCritters.MOD_ID, "block/soul_pot/soul_pot_cover_" + String.format("%02d", i))
            ));
            INSIDE_SPRITES.put(i, new SpriteIdentifier(
                    SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE,
                    Identifier.of(CropCritters.MOD_ID, "block/soul_pot/soul_pot_inside_" + String.format("%02d", i))
            ));
        }
    }
}