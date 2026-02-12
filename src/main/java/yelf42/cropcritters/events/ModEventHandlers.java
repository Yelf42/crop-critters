package yelf42.cropcritters.events;

import net.minecraft.block.*;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import yelf42.cropcritters.blocks.*;
import yelf42.cropcritters.items.ModItems;
import yelf42.cropcritters.sound.ModSounds;

// Converted from ModEvents, which was all FabricAPI callbacks
// Now functionality is found in Mixins
public class ModEventHandlers {

    public static boolean handleHoeUse(World world, BlockPos pos, BlockState state) {
        if (world.isClient()) {
            return (state.isOf(Blocks.SOUL_SOIL) || state.isOf(Blocks.SOUL_SAND));
        }

        // Soul Soil Tilling
        if (state.isOf(Blocks.SOUL_SOIL)) {
            world.setBlockState(pos, ModBlocks.SOUL_FARMLAND.getDefaultState(), Block.NOTIFY_ALL);
            world.playSound(null, pos, SoundEvents.ITEM_HOE_TILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
            return true;
        }

        // Soul Sand to Soil
        if (state.isOf(Blocks.SOUL_SAND)) {
            world.setBlockState(pos, Blocks.SOUL_SOIL.getDefaultState(), Block.NOTIFY_ALL);
            world.playSound(null, pos, SoundEvents.ITEM_HOE_TILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
            return true;
        }

        return false;
    }


    public static boolean handleShearsUse(PlayerEntity player, World world, ItemStack stack, BlockPos pos, BlockState state) {
        if (world.isClient()) {
            return (state.isOf(ModBlocks.STRANGLE_FERN)
                    || (state.isOf(ModBlocks.POPPER_PLANT) && state.get(PopperPlantBlock.AGE, 0) == PopperPlantBlock.MAX_AGE)
                    || (state.isOf(Blocks.BUSH))
                    || (state.isOf(ModBlocks.TALL_BUSH))
                    || (state.isOf(ModBlocks.ORNAMENTAL_BUSH)));
        }

        // Snip Strangle Fern
        if (state.isOf(ModBlocks.STRANGLE_FERN)) {
            StrangleFernBlockEntity sfbe = (StrangleFernBlockEntity) world.getBlockEntity(pos);
            BlockState infested = Blocks.DEAD_BUSH.getDefaultState();
            if (sfbe != null) {
                infested = sfbe.getInfestedState();
            }
            world.setBlockState(pos, infested, Block.NOTIFY_ALL);
            if (!player.isCreative()) stack.damage(1, player);
            world.playSound(null, pos, SoundEvents.ITEM_SHEARS_SNIP, SoundCategory.PLAYERS, 1.0F, 1.0F);
            return true;
        }

        // Harvest Popper Pod
        if (state.isOf(ModBlocks.POPPER_PLANT) && state.get(PopperPlantBlock.AGE, 0) == PopperPlantBlock.MAX_AGE) {
            Vec3d center = pos.toCenterPos();
            ItemStack itemStack = new ItemStack(ModItems.POPPER_POD);
            ItemEntity itemEntity = new ItemEntity(world, center.x, center.y, center.z, itemStack);
            itemEntity.setToDefaultPickupDelay();
            ((ServerWorld) world).spawnEntity(itemEntity);
            world.setBlockState(pos, state.with(PopperPlantBlock.AGE, 0));
            if (!player.isCreative()) stack.damage(1, player);
            world.playSound(null, pos, SoundEvents.ITEM_SHEARS_SNIP, SoundCategory.PLAYERS, 1.0F, 1.0F);
            return true;
        }

        // Trim Bush
        if (state.isOf(Blocks.BUSH)) {
            world.setBlockState(pos, Blocks.DEAD_BUSH.getDefaultState(), Block.NOTIFY_ALL);
            if (!player.isCreative()) stack.damage(1, player);
            world.playSound(null, pos, SoundEvents.ITEM_SHEARS_SNIP, SoundCategory.PLAYERS, 1.0F, 1.0F);
            return true;
        }

        // Trim Tall Bush
        if (state.isOf(ModBlocks.TALL_BUSH)) {
            BlockPos targetPos = (world.getBlockState(pos.down()).isOf(ModBlocks.TALL_BUSH)) ? pos.down() : pos;
            world.setBlockState(targetPos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
            world.setBlockState(targetPos.up(), Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
            TallPlantBlock.placeAt(world, ModBlocks.ORNAMENTAL_BUSH.getDefaultState(), targetPos, 3);
            if (!player.isCreative()) stack.damage(1, player);
            world.playSound(null, pos, SoundEvents.ITEM_SHEARS_SNIP, SoundCategory.PLAYERS, 1.0F, 1.0F);
            return true;
        }

        // Trim Ornamental Bush
        if (state.isOf(ModBlocks.ORNAMENTAL_BUSH)) {
            BlockPos targetPos = (world.getBlockState(pos.down()).isOf(ModBlocks.ORNAMENTAL_BUSH)) ? pos.down() : pos;
            world.setBlockState(targetPos, Blocks.DEAD_BUSH.getDefaultState(), Block.NOTIFY_ALL);
            if (!player.isCreative()) stack.damage(1, player);
            world.playSound(null, pos, SoundEvents.ITEM_SHEARS_SNIP, SoundCategory.PLAYERS, 1.0F, 1.0F);
            return true;
        }

        return false;
    }

    public static boolean handleStrangleFernPlanting(PlayerEntity player, World world, ItemStack stack, BlockPos pos, BlockState state) {
        if (world.isClient()) return false;

        BlockState toPlant = ModBlocks.STRANGLE_FERN.getDefaultState();
        if (toPlant.canPlaceAt(world, pos) && StrangleFern.canInfest(state)) {
            world.setBlockState(pos, toPlant);
            stack.decrementUnlessCreative(1, player);
            world.playSound(null, pos, ModSounds.SPORE_INFEST, SoundCategory.BLOCKS, 1.0F, 1.0F);
            return true;
        }

        return false;
    }

}
