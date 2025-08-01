package yelf42.cropcritters.renderer.entity;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.constant.dataticket.DataTicket;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;
import yelf42.cropcritters.CropCritters;
import yelf42.cropcritters.entity.AbstractCropCritterEntity;
import yelf42.cropcritters.model.entity.AbstractCritterModel;

public class AbstractCritterRenderer<R extends LivingEntityRenderState & GeoRenderState> extends GeoEntityRenderer<AbstractCropCritterEntity, R> {
    public static final DataTicket<Identifier> TEXTURE_PATH = DataTicket.create("critter_texture_path", Identifier.class);

    private final Identifier texture;
    private final Identifier trustingTexture;

    public AbstractCritterRenderer(EntityRendererFactory.Context context, Identifier id, boolean basicAnimation) {
        super(context, new AbstractCritterModel(id, basicAnimation));
        this.texture = Identifier.of(CropCritters.MOD_ID,"textures/entity/" + id.getPath() + ".png");
        this.trustingTexture = Identifier.of(CropCritters.MOD_ID,"textures/entity/" + id.getPath() + "_trusting.png");
    }

    @Override
    public Identifier getTextureLocation(R renderState) {
        return renderState.getOrDefaultGeckolibData(TEXTURE_PATH, texture);
    }

    @Override
    public void updateRenderState(AbstractCropCritterEntity entity, R entityRenderState, float partialTick) {
        super.updateRenderState(entity, entityRenderState, partialTick);
        entityRenderState.addGeckolibData(TEXTURE_PATH, entity.isTrusting() ? trustingTexture : texture);
        entityRenderState.addGeckolibData(DataTickets.IS_SHAKING, entity.isShaking());
    }

    @Override
    public void addRenderData(AbstractCropCritterEntity animatable, Void relatedObject, R renderState) {
        super.addRenderData(animatable, relatedObject, renderState);
        renderState.addGeckolibData(TEXTURE_PATH, animatable.isTrusting() ? trustingTexture : texture);
    }
}
