package yelf42.cropcritters.renderer.entity;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;
import yelf42.cropcritters.entity.WheatCritterEntity;
import yelf42.cropcritters.model.entity.WheatCritterModel;

public class WheatCritterRenderer <R extends LivingEntityRenderState & GeoRenderState> extends GeoEntityRenderer<WheatCritterEntity, R> {
    public WheatCritterRenderer(EntityRendererFactory.Context context) {
        super(context, new WheatCritterModel());
    }
}
