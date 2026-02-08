package yelf42.cropcritters.renderer.blockentity;

import net.minecraft.block.entity.DecoratedPotBlockEntity;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.util.math.Direction;
import org.jspecify.annotations.Nullable;

public class SoulPotBlockEntityRenderState extends BlockEntityRenderState {
    public DecoratedPotBlockEntity.@Nullable WobbleType wobbleType;
    public float wobbleAnimationProgress;
    public int level;
    public Direction facing;

    public SoulPotBlockEntityRenderState() {
        this.level = 0;
        this.facing = Direction.NORTH;
    }
}