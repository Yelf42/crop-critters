package yelf42.cropcritters.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;

@Environment(EnvType.CLIENT)
public class SoulGlintPlumeParticle extends BillboardParticle {
    private final SpriteProvider spriteProvider;

    protected SoulGlintPlumeParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, float scaleMultiplier, SpriteProvider spriteProvider) {
        super(world, x, y, z, 0.0F, 0.0F, 0.0F, spriteProvider.getFirst());
        this.velocityMultiplier = 0.96F;
        this.gravityStrength = 0.5F;
        this.ascending = true;
        this.spriteProvider = spriteProvider;
        this.velocityX *= 0.7F;
        this.velocityY += 0.15F;
        this.velocityY *= 0.6F;
        this.velocityZ *= 0.7F;
        this.velocityX += velocityX;
        this.velocityY += velocityY;
        this.velocityZ += velocityZ;
        this.scale *= 0.75F * scaleMultiplier;
        this.maxAge = (int)((double)7 / ((double)this.random.nextFloat() * 0.8 + 0.2) * (double)scaleMultiplier);
        this.maxAge = Math.max(this.maxAge, 1);
        this.updateSprite(spriteProvider);
        this.collidesWithWorld = false;
    }

    public BillboardParticle.RenderType getRenderType() {
        return RenderType.PARTICLE_ATLAS_OPAQUE;
    }

    public float getSize(float tickProgress) {
        return this.scale * MathHelper.clamp(((float)this.age + tickProgress) / (float)this.maxAge * 32.0F, 0.0F, 1.0F);
    }

    public void tick() {
        this.gravityStrength = 0.88F * this.gravityStrength;
        this.velocityMultiplier = 0.92F * this.velocityMultiplier;
        this.updateSprite(this.spriteProvider);
        super.tick();
    }

    @Environment(EnvType.CLIENT)
    public static class Factory implements ParticleFactory<SimpleParticleType> {
        private final SpriteProvider spriteProvider;

        public Factory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        public Particle createParticle(SimpleParticleType simpleParticleType, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i, Random random) {
            return new SoulGlintPlumeParticle(clientWorld, d, e, f, g, h, i, 1.0F, this.spriteProvider);
        }
    }
}