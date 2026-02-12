package yelf42.cropcritters.particle;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import yelf42.cropcritters.CropCritters;

public class ModParticles {

    public static final SimpleParticleType WATER_SPRAY =
            registerParticle("water_spray_particle", FabricParticleTypes.simple());

    public static final SimpleParticleType SOUL_SIPHON =
            registerParticle("soul_siphon_particle", FabricParticleTypes.simple());

    public static final ParticleType<ColorParticleOption> SPORES = registerTintedParticle("spore_particle");

    public static final SimpleParticleType SOUL_HEART =
            registerParticle("soul_heart_particle", FabricParticleTypes.simple());

    public static final SimpleParticleType SOUL_GLOW =
            registerParticle("soul_glow_particle", FabricParticleTypes.simple());

    public static final SimpleParticleType SOUL_GLINT =
            registerParticle("soul_glint_particle", FabricParticleTypes.simple());

    public static final SimpleParticleType SOUL_GLINT_PLUME =
            registerParticle("soul_glint_plume_particle", FabricParticleTypes.simple());

    private static SimpleParticleType registerParticle(String name, SimpleParticleType particleType) {
        return Registry.register(BuiltInRegistries.PARTICLE_TYPE, Identifier.fromNamespaceAndPath(CropCritters.MOD_ID, name), particleType);
    }

    private static ParticleType<ColorParticleOption> registerTintedParticle(String name) {
        return Registry.register(BuiltInRegistries.PARTICLE_TYPE, Identifier.fromNamespaceAndPath(CropCritters.MOD_ID, name), FabricParticleTypes.complex(ColorParticleOption::codec, ColorParticleOption::streamCodec));
    }

    public static void initialize() {
        CropCritters.LOGGER.info("Registering Particles for " + CropCritters.MOD_ID);
    }
}
