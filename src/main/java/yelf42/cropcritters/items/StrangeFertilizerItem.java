package yelf42.cropcritters.items;

import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BoneMealItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
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
import yelf42.cropcritters.blocks.SoulRoseBlock;
import yelf42.cropcritters.blocks.TallBushBlock;
import yelf42.cropcritters.sound.ModSounds;

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
            if (!world.isClient()) {
                if (playerEntity != null) playerEntity.emitGameEvent(GameEvent.ITEM_INTERACT_FINISH);
                world.syncWorldEvent(1505, blockPos2, 15);
            }

            return ActionResult.SUCCESS;
        }

        // Grow bush into tall bush
        if (useOnBush(context.getStack(), world, blockPos)) {
            return ActionResult.SUCCESS;
        }

        // Use on fertilizable things
        if (useOnFertilizable(context.getStack(), world, blockPos)) {
            if (!world.isClient()) {
                if (playerEntity != null) playerEntity.emitGameEvent(GameEvent.ITEM_INTERACT_FINISH);
                world.syncWorldEvent(1505, blockPos, 15);
            }

            return ActionResult.SUCCESS;
        }

        // Revive Coral
        if (tryReviveCoral(context.getStack(), world, blockPos, world.getBlockState(blockPos))) {
            if (playerEntity instanceof ServerPlayerEntity) {
                Criteria.ITEM_USED_ON_BLOCK.trigger((ServerPlayerEntity) playerEntity, blockPos, itemStack);
            }
            return ActionResult.SUCCESS;
        }

        // Trimmed Soul Rose
        if (blockState.isOf(ModBlocks.SOUL_ROSE) && blockState.get(SoulRoseBlock.LEVEL, 0) > 1) {
            if (!world.isClient()) {
                if (playerEntity != null) playerEntity.emitGameEvent(GameEvent.ITEM_INTERACT_FINISH);
                world.syncWorldEvent(1505, blockPos, 15);
                context.getStack().decrement(1);
                Block.dropStack(world, blockPos, new ItemStack(ModBlocks.TRIMMED_SOUL_ROSE));
            }
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    public static boolean useOnBush(ItemStack stack, World world, BlockPos blockPos) {
        BlockState state = world.getBlockState(blockPos);
        if (state.isOf(Blocks.BUSH) && world.getBlockState(blockPos.up()).isAir()) {
            TallBushBlock.placeAt(world, ModBlocks.TALL_BUSH.getDefaultState(), blockPos, 2);
            stack.decrement(1);
            return true;
        }
        return false;
    }

    public static boolean useOnGround(ItemStack stack, World world, BlockPos blockPos, BlockPos underwaterPos, @Nullable Direction facing) {
        BlockState state = world.getBlockState(blockPos);
        BlockState underwaterState = world.getBlockState(underwaterPos);
        if (underwaterState.isOf(Blocks.WATER) && world.getFluidState(underwaterPos).getLevel() == 8) {
            useUnderwater(stack, world, underwaterPos, facing);
            return true;
        } else if (state.isIn(BlockTags.DIRT) || state.getBlock() instanceof Fertilizable fertilizable && fertilizable.isFertilizable(world, blockPos, state)) {
            useOnLand(stack, world, blockPos);
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

            if (!blockState.isIn(CropCritters.IGNORE_STRANGE_FERTILIZERS) && blockState.canPlaceAt(world, blockPos2)) {
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

    private static void useOnLand(ItemStack stack, World world, BlockPos blockPos) {
        if (!(world instanceof ServerWorld)) return;

        Random random = world.getRandom();

        outerLoop:
        for (int i = 0; i < 128; i++) {
            BlockPos blockPos2 = blockPos;

            for (int j = 0; j < i / 16; j++) {
                blockPos2 = blockPos2.add(random.nextInt(3) - 1, (random.nextInt(3) - 1) * random.nextInt(3) / 2, random.nextInt(3) - 1);
                if (world.getBlockState(blockPos2).isFullCube(world, blockPos2)) {
                    continue outerLoop;
                }
            }

            BlockState blockState2 = world.getBlockState(blockPos2);
            if (blockState2.isOf(Blocks.AIR)) {
                BlockState toPlace;
                BlockState floor = world.getBlockState(blockPos2.down());
                if (floor.isOf(Blocks.CRIMSON_NYLIUM) || floor.isOf(Blocks.WARPED_NYLIUM)) {
                    toPlace = Registries.BLOCK
                            .getRandomEntry(CropCritters.ON_NYLIUM_STRANGE_FERTILIZERS, world.random)
                            .map(blockEntry -> ((Block)blockEntry.value()).getDefaultState())
                            .orElse(Blocks.NETHER_SPROUTS.getDefaultState());
                } else if (floor.isOf(Blocks.MYCELIUM)) {
                    toPlace = Registries.BLOCK
                            .getRandomEntry(CropCritters.ON_MYCELIUM_STRANGE_FERTILIZERS, world.random)
                            .map(blockEntry -> ((Block)blockEntry.value()).getDefaultState())
                            .orElse(Blocks.BROWN_MUSHROOM.getDefaultState());
                } else {
                    toPlace = Registries.BLOCK
                            .getRandomEntry((random.nextInt(4) == 0) ? CropCritters.ON_LAND_RARE_STRANGE_FERTILIZERS : CropCritters.ON_LAND_COMMON_STRANGE_FERTILIZERS, world.random)
                            .map(blockEntry -> ((Block)blockEntry.value()).getDefaultState())
                            .orElse(Blocks.SHORT_GRASS.getDefaultState());
                }

                if (!toPlace.isIn(CropCritters.IGNORE_STRANGE_FERTILIZERS) && toPlace.canPlaceAt(world, blockPos2)) {
                    if (toPlace.getBlock() instanceof TallPlantBlock) {
                        if (world.getBlockState(blockPos2.up()).isOf(Blocks.AIR)) {
                            TallPlantBlock.placeAt(world, toPlace, blockPos2, 3);
                        }
                    } else {
                        world.setBlockState(blockPos2, toPlace, Block.NOTIFY_ALL);
                    }
                }
            }
        }

        stack.decrement(1);
    }

    public static boolean tryReviveCoral(ItemStack stack, World world, BlockPos pos, BlockState state) {
        Optional<BlockState> optional = getRevivedState(state);
        if (optional.isPresent()) {
            world.setBlockState(pos, (BlockState) optional.get(), Block.NOTIFY_ALL_AND_REDRAW);
            world.emitGameEvent(null, GameEvent.BLOCK_CHANGE, pos);
            world.playSound(null, pos, ModSounds.REVIVE_CORAL, SoundCategory.BLOCKS, 1.0F, 1.0F);
            stack.decrement(1);
            return true;
        }
        return false;
    }

    private static Optional<BlockState> getRevivedState(BlockState state) {
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
