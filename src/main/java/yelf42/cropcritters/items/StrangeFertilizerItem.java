package yelf42.cropcritters.items;

import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BoneMealItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import com.google.common.collect.ImmutableMap.Builder;
import org.jetbrains.annotations.Nullable;
import yelf42.cropcritters.CropCritters;
import yelf42.cropcritters.blocks.ModBlocks;
import yelf42.cropcritters.blocks.TallBushBlock;

import java.util.Map;
import java.util.Optional;

public class StrangeFertilizerItem extends BoneMealItem {
    protected static final Map<Block, Block> REVIVE_CORAL = new Builder<Block, Block>()
            .put(Blocks.DEAD_TUBE_CORAL_BLOCK, Blocks.TUBE_CORAL_BLOCK)
            .put(Blocks.DEAD_TUBE_CORAL_WALL_FAN, Blocks.TUBE_CORAL_WALL_FAN)
            .put(Blocks.DEAD_TUBE_CORAL_FAN, Blocks.TUBE_CORAL_FAN)
            .put(Blocks.DEAD_TUBE_CORAL, Blocks.TUBE_CORAL)
            .put(Blocks.DEAD_BRAIN_CORAL_BLOCK, Blocks.BRAIN_CORAL_BLOCK)
            .put(Blocks.DEAD_BRAIN_CORAL_WALL_FAN, Blocks.BRAIN_CORAL_WALL_FAN)
            .put(Blocks.DEAD_BRAIN_CORAL_FAN, Blocks.BRAIN_CORAL_FAN)
            .put(Blocks.DEAD_BRAIN_CORAL, Blocks.BRAIN_CORAL)
            .put(Blocks.DEAD_BUBBLE_CORAL_BLOCK, Blocks.BUBBLE_CORAL_BLOCK)
            .put(Blocks.DEAD_BUBBLE_CORAL_WALL_FAN, Blocks.BUBBLE_CORAL_WALL_FAN)
            .put(Blocks.DEAD_BUBBLE_CORAL_FAN, Blocks.BUBBLE_CORAL_FAN)
            .put(Blocks.DEAD_BUBBLE_CORAL, Blocks.BUBBLE_CORAL)
            .put(Blocks.DEAD_FIRE_CORAL_BLOCK, Blocks.FIRE_CORAL_BLOCK)
            .put(Blocks.DEAD_FIRE_CORAL_WALL_FAN, Blocks.FIRE_CORAL_WALL_FAN)
            .put(Blocks.DEAD_FIRE_CORAL_FAN, Blocks.FIRE_CORAL_FAN)
            .put(Blocks.DEAD_FIRE_CORAL, Blocks.FIRE_CORAL)
            .put(Blocks.DEAD_HORN_CORAL_BLOCK, Blocks.HORN_CORAL_BLOCK)
            .put(Blocks.DEAD_HORN_CORAL_WALL_FAN, Blocks.HORN_CORAL_WALL_FAN)
            .put(Blocks.DEAD_HORN_CORAL_FAN, Blocks.HORN_CORAL_FAN)
            .put(Blocks.DEAD_HORN_CORAL, Blocks.HORN_CORAL)
            .build();


    public StrangeFertilizerItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos blockPos = context.getBlockPos();
        BlockPos blockPos2 = blockPos.offset(context.getSide());
        BlockState blockState = world.getBlockState(blockPos);
        PlayerEntity playerEntity = context.getPlayer();
        ItemStack itemStack = context.getStack();

        // Use on the ground
        boolean bl = blockState.isSideSolidFullSquare(world, blockPos, context.getSide());
        if (bl && useOnGround(context.getStack(), world, blockPos, blockPos2, context.getSide())) {
            if (!world.isClient) {
                context.getPlayer().emitGameEvent(GameEvent.ITEM_INTERACT_FINISH);
                world.syncWorldEvent(1505, blockPos2, 15);
            }

            return ActionResult.SUCCESS;
        }

        // Grow bush into tall bush
        if (blockState.isOf(Blocks.BUSH) && world.getBlockState(blockPos.up()).isAir()) {
            TallBushBlock.placeAt(world, ModBlocks.TALL_BUSH.getDefaultState(), blockPos, 2);

            if (playerEntity != null) {
                itemStack.decrement(1);
            }

            return ActionResult.SUCCESS;
        }

        // Use on fertilizable things
        if (useOnFertilizable(context.getStack(), world, blockPos)) {
            if (!world.isClient) {
                context.getPlayer().emitGameEvent(GameEvent.ITEM_INTERACT_FINISH);
                world.syncWorldEvent(1505, blockPos, 15);
            }

            return ActionResult.SUCCESS;
        }

        // Revive Coral
        Optional<BlockState> optional = this.tryReviveCoral(world, blockPos, playerEntity, world.getBlockState(blockPos));
        if (optional.isEmpty()) {
            return ActionResult.PASS;
        } else {
            if (playerEntity instanceof ServerPlayerEntity) {
                Criteria.ITEM_USED_ON_BLOCK.trigger((ServerPlayerEntity) playerEntity, blockPos, itemStack);
            }

            world.setBlockState(blockPos, (BlockState) optional.get(), Block.NOTIFY_ALL_AND_REDRAW);
            world.emitGameEvent(GameEvent.BLOCK_CHANGE, blockPos, GameEvent.Emitter.of(playerEntity, (BlockState) optional.get()));
            if (playerEntity != null) {
                itemStack.damage(1, playerEntity, LivingEntity.getSlotForHand(context.getHand()));
            }

            return ActionResult.SUCCESS;
        }
    }

    // Force grow trees?
    public static boolean useOnFertilizable(ItemStack stack, World world, BlockPos pos) {
        BlockState blockState = world.getBlockState(pos);
        if (blockState.getBlock() instanceof Fertilizable fertilizable && fertilizable.isFertilizable(world, pos, blockState)) {
            if (world instanceof ServerWorld) {
                if (fertilizable.canGrow(world, world.random, pos, blockState)) {
                    fertilizable.grow((ServerWorld)world, world.random, pos, blockState);
                }
                stack.decrement(1);
            }
            return true;
        } else {
            return false;
        }
    }

    // More random / larger than bonemeal effect
    public static boolean useOnGround(ItemStack stack, World world, BlockPos blockPos, BlockPos underwaterPos, @Nullable Direction facing) {
        BlockState state = world.getBlockState(blockPos);
        BlockState underwaterState = world.getBlockState(underwaterPos);
        if (underwaterState.isOf(Blocks.WATER) && world.getFluidState(underwaterPos).getLevel() == 8) {
            useUnderwater(stack, world, underwaterPos, facing);
            return true;
        } else if (state.isIn(BlockTags.DIRT) || state.getBlock() instanceof Fertilizable fertilizable && fertilizable.isFertilizable(world, blockPos, state)) {
            useOnLand(stack, world, blockPos, facing);
            return true;
        } else {
            return false;
        }
    }

    private static void useUnderwater(ItemStack stack, World world, BlockPos blockPos, @Nullable Direction facing) {
        if (!(world instanceof ServerWorld)) return;

        blockPos = blockPos.offset(facing);
        Random random = world.getRandom();

        label80:
        for (int i = 0; i < 128; i++) {
            BlockPos blockPos2 = blockPos;
            BlockState blockState = Blocks.SEAGRASS.getDefaultState();

            for (int j = 0; j < i / 16; j++) {
                blockPos2 = blockPos2.add(random.nextInt(3) - 1, (random.nextInt(3) - 1) * random.nextInt(3) / 2, random.nextInt(3) - 1);
                if (world.getBlockState(blockPos2).isFullCube(world, blockPos2)) {
                    continue label80;
                }
            }

            if (i == 0 && facing != null && facing.getAxis().isHorizontal()) {
                blockState = (BlockState) Registries.BLOCK
                        .getRandomEntry(BlockTags.WALL_CORALS, world.random)
                        .map(blockEntry -> ((Block)blockEntry.value()).getDefaultState())
                        .orElse(blockState);
                if (blockState.contains(DeadCoralWallFanBlock.FACING)) {
                    blockState = blockState.with(DeadCoralWallFanBlock.FACING, facing);
                }
            } else if (random.nextInt(2) == 0) {
                blockState = (BlockState)Registries.BLOCK
                        .getRandomEntry(CropCritters.UNDERWATER_STRANGE_FERTILIZERS, world.random)
                        .map(blockEntry -> ((Block)blockEntry.value()).getDefaultState())
                        .orElse(blockState);
            }

            if (blockState.isIn(BlockTags.WALL_CORALS, state -> state.contains(DeadCoralWallFanBlock.FACING))) {
                for (int k = 0; !blockState.canPlaceAt(world, blockPos2) && k < 4; k++) {
                    blockState = blockState.with(DeadCoralWallFanBlock.FACING, Direction.Type.HORIZONTAL.random(random));
                }
            }

            if (blockState.canPlaceAt(world, blockPos2)) {
                BlockState blockState2 = world.getBlockState(blockPos2);
                if (blockState2.isOf(Blocks.WATER) && world.getFluidState(blockPos2).getLevel() == 8) {
                    world.setBlockState(blockPos2, blockState, Block.NOTIFY_ALL);
                } else if (random.nextInt(2) == 0 && !blockState2.isIn(BlockTags.SAPLINGS)) {
                    useOnFertilizable(stack, world, blockPos2);
                }
            }
        }

        stack.decrement(1);
    }

    private static void useOnLand(ItemStack stack, World world, BlockPos blockPos, @Nullable Direction facing) {
        if (!(world instanceof ServerWorld)) return;

        Random random = world.getRandom();

        label980:
        for (int i = 0; i < 128; i++) {
            BlockPos blockPos2 = blockPos;
            BlockState blockState = Blocks.SHORT_GRASS.getDefaultState();

            for (int j = 0; j < i / 16; j++) {
                blockPos2 = blockPos2.add(random.nextInt(3) - 1, (random.nextInt(3) - 1) * random.nextInt(3) / 2, random.nextInt(3) - 1);
                if (world.getBlockState(blockPos2).isFullCube(world, blockPos2)) {
                    continue label980;
                }
            }

            BlockState blockState2 = world.getBlockState(blockPos2);
            if (blockState2.isOf(Blocks.AIR)) {
                BlockState floor = world.getBlockState(blockPos2.down());

                blockState = (floor.isOf(Blocks.CRIMSON_NYLIUM) || floor.isOf(Blocks.WARPED_NYLIUM)) ?
                        (BlockState)Registries.BLOCK
                        .getRandomEntry(CropCritters.ON_NYLIUM_STRANGE_FERTILIZERS, world.random)
                        .map(blockEntry -> ((Block)blockEntry.value()).getDefaultState())
                        .orElse(blockState)
                        :
                        (BlockState)Registries.BLOCK
                        .getRandomEntry((random.nextInt(4) == 0) ? CropCritters.ON_LAND_RARE_STRANGE_FERTILIZERS : CropCritters.ON_LAND_COMMON_STRANGE_FERTILIZERS, world.random)
                        .map(blockEntry -> ((Block)blockEntry.value()).getDefaultState())
                        .orElse(blockState);

                if (!blockState.isIn(CropCritters.IGNORE_STRANGE_FERTILIZERS) && blockState.canPlaceAt(world, blockPos2)) {
                    if (blockState.getBlock() instanceof TallPlantBlock && world.getBlockState(blockPos2.up()).isOf(Blocks.AIR)) {
                        TallPlantBlock.placeAt(world, blockState, blockPos2, 3);
                    } else {
                        world.setBlockState(blockPos2, blockState, Block.NOTIFY_ALL);
                    }
                }
            }
        }

        stack.decrement(1);
    }

    private Optional<BlockState> tryReviveCoral(World world, BlockPos pos, @Nullable PlayerEntity player, BlockState state) {
        Optional<BlockState> optional = this.getRevivedState(state);
        if (optional.isPresent()) {
            world.playSound(player, pos, SoundEvents.ITEM_AXE_STRIP, SoundCategory.BLOCKS, 1.0F, 1.0F);
            return optional;
        }
        return Optional.empty();
    }

    private Optional<BlockState> getRevivedState(BlockState state) {
        return Optional.ofNullable((Block)REVIVE_CORAL.get(state.getBlock()))
                .map(block -> {
                    BlockState revivedState = block.getDefaultState();
                    //block.getDefaultState().with(Properties.WATERLOGGED, state.get(Properties.WATERLOGGED))
                    if (state.contains(Properties.HORIZONTAL_FACING)) {
                        revivedState = revivedState.with(Properties.HORIZONTAL_FACING, state.get(Properties.HORIZONTAL_FACING));
                    }

                    if (state.contains(Properties.WATERLOGGED)) {
                        revivedState = revivedState.with(Properties.WATERLOGGED, state.get(Properties.WATERLOGGED));
                    }

                    return revivedState;
                });
    }

}
