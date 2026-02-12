package yelf42.cropcritters.renderer.blockentity;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.item.model.special.SpecialModelRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3fc;

public class SoulPotModelRenderer implements SpecialModelRenderer<Integer> {
    private final SoulPotBlockEntityRenderer blockEntityRenderer;

    public SoulPotModelRenderer(SoulPotBlockEntityRenderer blockEntityRenderer) {
        this.blockEntityRenderer = blockEntityRenderer;
    }

    @Override
    public @Nullable Integer getData(ItemStack itemStack) {
        // Always return 0 for items since they're empty
        return 0;
    }

    @Override
    public void render(@Nullable Integer level, ItemDisplayContext itemDisplayContext, MatrixStack matrixStack,
                       OrderedRenderCommandQueue orderedRenderCommandQueue, int light, int overlay, boolean bl, int k) {
        // Render with level 0 (or the provided level, which will always be 0)
        this.blockEntityRenderer.render(matrixStack, orderedRenderCommandQueue, light, overlay, k, level != null ? level : 0);
    }

    @Override
    public void collectVertices(java.util.function.Consumer<Vector3fc> consumer) {
        this.blockEntityRenderer.collectVertices(consumer);
    }

    public record Unbaked() implements SpecialModelRenderer.Unbaked {
        public static final MapCodec<Unbaked> CODEC = MapCodec.unit(new Unbaked());

        @Override
        public MapCodec<Unbaked> getCodec() {
            return CODEC;
        }

        @Override
        public SpecialModelRenderer<?> bake(SpecialModelRenderer.BakeContext context) {
            return new SoulPotModelRenderer(new SoulPotBlockEntityRenderer(context));
        }
    }
}
