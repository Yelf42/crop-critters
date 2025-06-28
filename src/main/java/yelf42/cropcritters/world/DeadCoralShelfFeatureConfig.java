package yelf42.cropcritters.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.world.gen.feature.FeatureConfig;

public record DeadCoralShelfFeatureConfig(int number, Identifier blockId) implements FeatureConfig {
    public static final Codec<DeadCoralShelfFeatureConfig> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                            // you can add as many of these as you want, one for each parameter
                            Codecs.POSITIVE_INT.fieldOf("number").forGetter(DeadCoralShelfFeatureConfig::number),
                            Identifier.CODEC.fieldOf("blockID").forGetter(DeadCoralShelfFeatureConfig::blockId))
                    .apply(instance, DeadCoralShelfFeatureConfig::new));
}
