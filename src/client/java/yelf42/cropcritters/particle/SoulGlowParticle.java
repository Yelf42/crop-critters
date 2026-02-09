package yelf42.cropcritters.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;

@Environment(EnvType.CLIENT)
public class SoulGlowParticle extends BillboardParticle {

    private final SpriteProvider spriteProvider;

    SoulGlowParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, SpriteProvider spriteProvider) {
        super(world, x, y, z, velocityX, velocityY, velocityZ, spriteProvider.getFirst());
        this.velocityMultiplier = 0.96F;
        this.ascending = true;
        this.spriteProvider = spriteProvider;
        this.scale *= 0.75F;
        this.collidesWithWorld = false;
        this.maxAge *= 2;
        this.updateSprite(spriteProvider);
    }

    public BillboardParticle.RenderType getRenderType() {
        return RenderType.PARTICLE_ATLAS_TRANSLUCENT;
    }

    public int getBrightness(float tint) {
        float f = ((float)this.age + tint) / (float)this.maxAge;
        f = MathHelper.clamp(f, 0.0F, 1.0F);
        int i = super.getBrightness(tint);
        int j = i & 255;
        int k = i >> 16 & 255;
        j += (int)(f * 15.0F * 16.0F);
        if (j > 240) {
            j = 240;
        }

        return j | k << 16;
    }

    public void tick() {
        super.tick();
        this.updateSprite(this.spriteProvider);
    }

    @Environment(EnvType.CLIENT)
    public static class Factory implements ParticleFactory<SimpleParticleType> {
        private final SpriteProvider spriteProvider;

        public Factory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        public Particle createParticle(SimpleParticleType simpleParticleType, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i, Random random) {
            SoulGlowParticle glowParticle = new SoulGlowParticle(clientWorld, d, e, f, (double)0.5F - random.nextDouble(), h, (double)0.5F - random.nextDouble(), this.spriteProvider);

            glowParticle.velocityY *= (double)0.2F;
            if (g == (double)0.0F && i == (double)0.0F) {
                glowParticle.velocityX *= (double)0.1F;
                glowParticle.velocityZ *= (double)0.1F;
            }

            glowParticle.setMaxAge((int)((double)8.0F / (random.nextDouble() * 0.8 + 0.2)));
            return glowParticle;
        }
    }

}
