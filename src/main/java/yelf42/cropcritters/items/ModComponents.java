package yelf42.cropcritters.items;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.item.v1.ComponentTooltipAppenderRegistry;
import net.minecraft.component.ComponentType;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.tooltip.TooltipAppender;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import yelf42.cropcritters.CropCritters;

import java.util.List;
import java.util.function.Consumer;

public class ModComponents {
    public static void initialize() {
        CropCritters.LOGGER.info("Initializing components for " + CropCritters.MOD_ID);

        ComponentTooltipAppenderRegistry.addAfter(
                DataComponentTypes.DAMAGE,
                POISONOUS_SEED_BALL
        );
        ComponentTooltipAppenderRegistry.addAfter(
                POISONOUS_SEED_BALL,
                SEED_TYPES
        );
    }

    // Item components
    public static final ComponentType<SeedTypesComponent> SEED_TYPES = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            Identifier.of(CropCritters.MOD_ID, "seed_types"),
            ComponentType.<SeedTypesComponent>builder().codec(SeedTypesComponent.CODEC).build()
    );
    public record SeedTypesComponent(List<Identifier> seedTypes) implements TooltipAppender {
        public static final Codec<SeedTypesComponent> CODEC = RecordCodecBuilder.create(builder -> {
            return builder.group(
                    Identifier.CODEC.listOf().fieldOf("seedTypes").forGetter(SeedTypesComponent::seedTypes)
            ).apply(builder, SeedTypesComponent::new);
        });

        @Override
        public void appendTooltip(Item.TooltipContext context, Consumer<Text> tooltip, TooltipType type, ComponentsAccess components) {
            for (Identifier seedType : seedTypes) {
                tooltip.accept(Text.literal(" - ").append(Text.translatable("block." + seedType.getNamespace() + "." + seedType.getPath()).formatted(Formatting.GRAY)));
            }
        }
    }

    public static final ComponentType<PoisonousComponent> POISONOUS_SEED_BALL = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            Identifier.of(CropCritters.MOD_ID, "poisonous_seed_ball"),
            ComponentType.<PoisonousComponent>builder().codec(PoisonousComponent.CODEC).build()
    );
    public record PoisonousComponent(int poisonStacks) implements TooltipAppender {
        public static final Codec<PoisonousComponent> CODEC = RecordCodecBuilder.create(builder -> {
            return builder.group(
                    Codec.INT.fieldOf("poisonStacks").forGetter(PoisonousComponent::poisonStacks)
            ).apply(builder, PoisonousComponent::new);
        });

        @Override
        public void appendTooltip(Item.TooltipContext context, Consumer<Text> tooltip, TooltipType type, ComponentsAccess components) {
            if (poisonStacks > 0) tooltip.accept(Text.translatable("item.cropcritters.tooltip.poisonous_seed_ball").formatted(Formatting.GREEN));
        }
    }

}
