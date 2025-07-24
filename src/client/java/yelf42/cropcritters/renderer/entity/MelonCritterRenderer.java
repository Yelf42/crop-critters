package yelf42.cropcritters.renderer.entity;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.particle.ParticleTypes;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;
import yelf42.cropcritters.entity.MelonCritterEntity;
import yelf42.cropcritters.entity.WheatCritterEntity;
import yelf42.cropcritters.model.entity.MelonCritterModel;
import yelf42.cropcritters.model.entity.WheatCritterModel;

public class MelonCritterRenderer<R extends LivingEntityRenderState & GeoRenderState> extends GeoEntityRenderer<MelonCritterEntity, R> {
    public MelonCritterRenderer(EntityRendererFactory.Context context) {
        super(context, new MelonCritterModel());
    }
}
