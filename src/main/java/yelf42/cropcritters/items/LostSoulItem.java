package yelf42.cropcritters.items;

import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class LostSoulItem extends Item {

    public LostSoulItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos blockPos = context.getBlockPos();
        BlockState state = world.getBlockState(blockPos);
        ItemStack itemStack = context.getStack();

        if (state.isOf(Blocks.PUMPKIN)) {
            // TODO spawn pumpkin critter
        } else if (state.isOf(Blocks.MELON)) {
            // TODO spawn melon critter
        } else if (state.isOf(Blocks.COCOA)) {
            // TODO spawn cocoa critter
        } else if (state.getBlock() instanceof PlantBlock) {
            if (state.isOf(Blocks.PITCHER_PLANT)) {
                // TODO spawn pitcher critter
            } else if (state.isOf(Blocks.TORCHFLOWER)) {
                // TODO spawn torchflower critter
            } else if (state.isOf(Blocks.NETHER_WART)) {
                // TODO spawn nether wart critter
            } else if (state.getBlock() instanceof CropBlock cropBlock && cropBlock.isMature(state)) {
                if (state.isOf(Blocks.WHEAT)) {
                    // TODO spawn wheat critter
                } else if (state.isOf(Blocks.CARROTS)) {
                    // TODO spawn carrot critter
                } else if (state.isOf(Blocks.POTATOES)) {
                    // TODO spawn potato critter
                    // poisonous potato critter
                } else if (state.isOf(Blocks.BEETROOTS)) {
                    // TODO spawn beetroot critter
                } else {
                    return ActionResult.PASS;
                }
            } else {
                return ActionResult.PASS;
            }
        } else {
            return ActionResult.PASS;
        }

        // TODO particles and sfx for spawning
        world.setBlockState(blockPos, Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
        PlayerEntity playerEntity = context.getPlayer();
        if (playerEntity instanceof ServerPlayerEntity serverPlayerEntity) {
            Criteria.ITEM_USED_ON_BLOCK.trigger(serverPlayerEntity, blockPos, itemStack);
        }
        itemStack.decrement(1);
        return ActionResult.SUCCESS;
    }
}
