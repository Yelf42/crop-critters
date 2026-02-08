package yelf42.cropcritters.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.inventory.LootableInventory;
import net.minecraft.inventory.SingleStackInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.state.property.Properties;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class SoulPotBlockEntity extends BlockEntity implements LootableInventory, SingleStackInventory.SingleStackBlockEntityInventory {
    public long lastWobbleTime;
    public @Nullable WobbleType lastWobbleType;
    private ItemStack stack;
    protected @Nullable RegistryKey<LootTable> lootTableId;
    protected long lootTableSeed;

    public SoulPotBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SOUL_POT, pos, state);
        this.stack = ItemStack.EMPTY;
    }

    protected void writeData(WriteView view) {
        super.writeData(view);
        if (!this.writeLootTable(view) && !this.stack.isEmpty()) {
            view.put("item", ItemStack.CODEC, this.stack);
        }

    }

    protected void readData(ReadView view) {
        super.readData(view);
        if (!this.readLootTable(view)) {
            this.stack = view.read("item", ItemStack.CODEC).orElse(ItemStack.EMPTY);
        } else {
            this.stack = ItemStack.EMPTY;
        }

    }

    public BlockEntityUpdateS2CPacket toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registries) {
        return this.createComponentlessNbt(registries);
    }

    public Direction getHorizontalFacing() {
        return this.getCachedState().get(Properties.HORIZONTAL_FACING);
    }

    public @Nullable RegistryKey<LootTable> getLootTable() {
        return this.lootTableId;
    }

    public void setLootTable(@Nullable RegistryKey<LootTable> lootTable) {
        this.lootTableId = lootTable;
    }

    public long getLootTableSeed() {
        return this.lootTableSeed;
    }

    public void setLootTableSeed(long lootTableSeed) {
        this.lootTableSeed = lootTableSeed;
    }

    protected void addComponents(ComponentMap.Builder builder) {
        super.addComponents(builder);
        builder.add(DataComponentTypes.CONTAINER, ContainerComponent.fromStacks(List.of(this.stack)));
    }

    protected void readComponents(ComponentsAccess components) {
        super.readComponents(components);
        this.stack = (components.getOrDefault(DataComponentTypes.CONTAINER, ContainerComponent.DEFAULT)).copyFirstStack();
    }

    public void removeFromCopiedStackData(WriteView view) {
        super.removeFromCopiedStackData(view);
        view.remove("item");
    }

    public ItemStack getStack() {
        this.generateLoot(null);
        return this.stack;
    }

    public ItemStack decreaseStack(int count) {
        this.generateLoot(null);
        ItemStack itemStack = this.stack.split(count);
        if (this.stack.isEmpty()) {
            this.stack = ItemStack.EMPTY;
        }

        this.markDirty();
        if (this.world != null && !this.world.isClient()) {
            this.world.updateListeners(this.pos, this.getCachedState(), this.getCachedState(), 3);
        }

        return itemStack;
    }

    public void increaseStack() {
        this.generateLoot(null);
        this.stack.increment(1);

        this.markDirty();
        if (this.world != null && !this.world.isClient()) {
            this.world.updateListeners(this.pos, this.getCachedState(), this.getCachedState(), 3);
        }
    }

    public void setStack(ItemStack stack) {
        this.generateLoot(null);
        this.stack = stack;

        this.markDirty();
        if (this.world != null && !this.world.isClient()) {
            this.world.updateListeners(this.pos, this.getCachedState(), this.getCachedState(), 3);
        }
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        return false;
    }

    public BlockEntity asBlockEntity() {
        return this;
    }

    public void wobble(WobbleType wobbleType) {
        if (this.world != null && !this.world.isClient()) {
            this.world.addSyncedBlockEvent(this.getPos(), this.getCachedState().getBlock(), 1, wobbleType.ordinal());
        }
    }

    public boolean onSyncedBlockEvent(int type, int data) {
        if (this.world != null && type == 1 && data >= 0 && data < WobbleType.values().length) {
            this.lastWobbleTime = this.world.getTime();
            this.lastWobbleType = WobbleType.values()[data];
            return true;
        } else {
            return super.onSyncedBlockEvent(type, data);
        }
    }

    public static enum WobbleType {
        POSITIVE(7),
        NEGATIVE(10);

        public final int lengthInTicks;

        private WobbleType(final int lengthInTicks) {
            this.lengthInTicks = lengthInTicks;
        }
    }
}