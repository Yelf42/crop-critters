package yelf42.cropcritters.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.TintedParticleEffect;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;

public class SporeParticle extends BillboardParticle {
    private static final Random RANDOM = Random.create();
    private final SpriteProvider spriteProvider;
    private float defaultAlpha = 1.0F;

    SporeParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, SpriteProvider spriteProvider) {
        super(world, x, y, z, (double) 0.5F - RANDOM.nextDouble(), velocityY, (double) 0.5F - RANDOM.nextDouble(), spriteProvider.getFirst());
        this.velocityMultiplier = 0.96F;
        this.gravityStrength = 0.1F;
        this.ascending = false;
        this.spriteProvider = spriteProvider;
        this.velocityY *= (double) -0.1F;
        if (velocityX == (double) 0.0F && velocityZ == (double) 0.0F) {
            this.velocityX *= (double) 0.1F;
            this.velocityZ *= (double) 0.1F;
        }

        this.scale *= 0.75F;
        this.maxAge = (int) ((double) 5.0F / ((double) this.random.nextFloat() * 0.8 + 0.2));
        this.collidesWithWorld = false;
        this.updateSprite(spriteProvider);
        if (this.isInvisible()) {
            this.setAlpha(0.0F);
        }

    }

    public BillboardParticle.RenderType getRenderType() {
        return RenderType.PARTICLE_ATLAS_TRANSLUCENT;
    }

    public void tick() {
        super.tick();
        this.updateSprite(this.spriteProvider);
        if (this.isInvisible()) {
            this.alpha = 0.0F;
        } else {
            this.alpha = MathHelper.lerp(0.05F, this.alpha, this.defaultAlpha);
        }

    }

    protected void setAlpha(float alpha) {
        super.setAlpha(alpha);
        this.defaultAlpha = alpha;
    }

    private boolean isInvisible() {
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        ClientPlayerEntity clientPlayerEntity = minecraftClient.player;
        return clientPlayerEntity != null && clientPlayerEntity.getEyePos().squaredDistanceTo(this.x, this.y, this.z) <= (double) 9.0F && minecraftClient.options.getPerspective().isFirstPerson() && clientPlayerEntity.isUsingSpyglass();
    }

    @Environment(EnvType.CLIENT)
    public static class Factory implements ParticleFactory<TintedParticleEffect> {
        private final SpriteProvider spriteProvider;

        public Factory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        public Particle createParticle(TintedParticleEffect tintedParticleEffect, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i, Random random) {
            SporeParticle sporeParticle = new SporeParticle(clientWorld, d, e, f, g, h, i, this.spriteProvider);
            sporeParticle.setColor(tintedParticleEffect.getRed(), tintedParticleEffect.getGreen(), tintedParticleEffect.getBlue());
            sporeParticle.setAlpha(tintedParticleEffect.getAlpha());
            return sporeParticle;
        }
    }
}