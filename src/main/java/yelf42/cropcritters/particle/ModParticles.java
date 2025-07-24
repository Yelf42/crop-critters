package yelf42.cropcritters.particle;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import yelf42.cropcritters.CropCritters;

public class ModParticles {

    public static final SimpleParticleType WATER_SPRAY_PARTICLE =
            registerParticle("water_spray_particle", FabricParticleTypes.simple());

    private static SimpleParticleType registerParticle(String name, SimpleParticleType particleType) {
        return Registry.register(Registries.PARTICLE_TYPE, Identifier.of(CropCritters.MOD_ID, name), particleType);
    }

    public static void initialize() {
        CropCritters.LOGGER.info("Registering Particles for " + CropCritters.MOD_ID);
    }
}
