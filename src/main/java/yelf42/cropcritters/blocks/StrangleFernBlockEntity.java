package yelf42.cropcritters.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;

public class StrangleFernBlockEntity extends BlockEntity {
    private BlockState infestedState = Blocks.DEAD_BUSH.getDefaultState();

    public StrangleFernBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.STRANGLE_FERN, pos, state);
    }

    @Override
    protected void readData(ReadView view) {
        super.readData(view);
        infestedState = view.read("InfestedState", BlockState.CODEC).orElse(Blocks.DEAD_BUSH.getDefaultState());
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        view.put("InfestedState", BlockState.CODEC, infestedState);
    }

    public BlockState getInfestedState() {
        return infestedState;
    }

    public void setInfestedState(BlockState state) {
        infestedState = state;
        updateListeners();
    }

    public BlockEntityUpdateS2CPacket toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registries) {
        return this.createComponentlessNbt(registries);
    }

    private void updateListeners() {
        this.markDirty();
        this.world.updateListeners(this.getPos(), this.getCachedState(), this.getCachedState(), 3);
    }
}
