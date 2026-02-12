package yelf42.cropcritters.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import yelf42.cropcritters.CropCritters;

public class ModEffects {

    public static void initialize() {
        CropCritters.LOGGER.info("Initializing effects for " + CropCritters.MOD_ID);
    }

    public static final Holder<MobEffect> SPORES = register("spores_effect", new SporesEffect(MobEffectCategory.NEUTRAL, 5882118));
    public static final MobEffectInstance NATURAL_SPORES = new MobEffectInstance(SPORES, 6000, 0, true, true, false);

    public static final Holder<MobEffect> PUFFBOMB_POISONING = register("puffbomb_poisoning", new PuffbombPoisoningEffect(MobEffectCategory.HARMFUL, 16770790));
    public static final MobEffectInstance EATEN_PUFFBOMB_POISONING = new MobEffectInstance(PUFFBOMB_POISONING, 2400, 0, false, false, true);

    public static final Holder<MobEffect> SOUL_SIPHON = register("soul_siphon", new SoulSiphonEffect(MobEffectCategory.HARMFUL, 7561558));



    private static Holder<MobEffect> register(String id, MobEffect statusEffect) {
        return Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, Identifier.fromNamespaceAndPath(CropCritters.MOD_ID, id), statusEffect);
    }
}
