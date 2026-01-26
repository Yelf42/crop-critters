package yelf42.cropcritters.effects;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import yelf42.cropcritters.CropCritters;

public class ModEffects {

    public static void initialize() {
        CropCritters.LOGGER.info("Initializing effects for " + CropCritters.MOD_ID);
    }

    public static final RegistryEntry<StatusEffect> SPORES = register("spores_effect", new SporesEffect(StatusEffectCategory.NEUTRAL, 5882118));
    public static final StatusEffectInstance NATURAL_SPORES = new StatusEffectInstance(SPORES, 6000, 0, true, true, false);

    public static final RegistryEntry<StatusEffect> PUFFBOMB_POISONING = register("puffbomb_poisoning", new PuffbombPoisoningEffect(StatusEffectCategory.HARMFUL, 16770790));
    public static final StatusEffectInstance EATEN_PUFFBOMB_POISONING = new StatusEffectInstance(PUFFBOMB_POISONING, 2400, 0, false, false, true);


    private static RegistryEntry<StatusEffect> register(String id, StatusEffect statusEffect) {
        return Registry.registerReference(Registries.STATUS_EFFECT, Identifier.of(CropCritters.MOD_ID, id), statusEffect);
    }
}
