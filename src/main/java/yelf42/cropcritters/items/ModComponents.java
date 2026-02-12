package yelf42.cropcritters.items;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.item.v1.ComponentTooltipAppenderRegistry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.TooltipProvider;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.Identifier;
import yelf42.cropcritters.CropCritters;

import java.util.List;
import java.util.function.Consumer;

public class ModComponents {
    public static void initialize() {
        CropCritters.LOGGER.info("Initializing components for " + CropCritters.MOD_ID);

        ComponentTooltipAppenderRegistry.addAfter(
                DataComponents.DAMAGE,
                POISONOUS_SEED_BALL
        );
        ComponentTooltipAppenderRegistry.addAfter(
                POISONOUS_SEED_BALL,
                SEED_TYPES
        );
    }

    // Item components
    public static final DataComponentType<SeedTypesComponent> SEED_TYPES = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            Identifier.fromNamespaceAndPath(CropCritters.MOD_ID, "seed_types"),
            DataComponentType.<SeedTypesComponent>builder().persistent(SeedTypesComponent.CODEC).build()
    );
    public record SeedTypesComponent(List<Identifier> seedTypes) implements TooltipProvider {
        public static final Codec<SeedTypesComponent> CODEC = RecordCodecBuilder.create(builder -> {
            return builder.group(
                    Identifier.CODEC.listOf().fieldOf("seedTypes").forGetter(SeedTypesComponent::seedTypes)
            ).apply(builder, SeedTypesComponent::new);
        });

        @Override
        public void addToTooltip(Item.TooltipContext context, Consumer<Component> tooltip, TooltipFlag type, DataComponentGetter components) {
            for (Identifier seedType : seedTypes) {
                tooltip.accept(Component.literal(" - ").append(Component.translatable("block." + seedType.getNamespace() + "." + seedType.getPath()).withStyle(ChatFormatting.GRAY)));
            }
        }
    }

    public static final DataComponentType<PoisonousComponent> POISONOUS_SEED_BALL = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            Identifier.fromNamespaceAndPath(CropCritters.MOD_ID, "poisonous_seed_ball"),
            DataComponentType.<PoisonousComponent>builder().persistent(PoisonousComponent.CODEC).build()
    );
    public record PoisonousComponent(int poisonStacks) implements TooltipProvider {
        public static final Codec<PoisonousComponent> CODEC = RecordCodecBuilder.create(builder -> {
            return builder.group(
                    Codec.INT.fieldOf("poisonStacks").forGetter(PoisonousComponent::poisonStacks)
            ).apply(builder, PoisonousComponent::new);
        });

        @Override
        public void addToTooltip(Item.TooltipContext context, Consumer<Component> tooltip, TooltipFlag type, DataComponentGetter components) {
            if (poisonStacks > 0) tooltip.accept(Component.translatable("item.cropcritters.tooltip.poisonous_seed_ball").append(CropCritters.INT_TO_ROMAN[poisonStacks]).withStyle(ChatFormatting.GREEN));
        }
    }

}
