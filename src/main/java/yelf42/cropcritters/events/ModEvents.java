package yelf42.cropcritters.events;

import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.TallPlantBlock;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import yelf42.cropcritters.CropCritters;
import yelf42.cropcritters.blocks.ModBlocks;
import yelf42.cropcritters.config.ConfigManager;
import yelf42.cropcritters.items.ModItems;

import java.util.concurrent.ThreadLocalRandom;

public class ModEvents {
    public static void initialize() {
        CropCritters.LOGGER.info("Initializing events for " + CropCritters.MOD_ID);
        registerSoulSoilTilling();
        registerSoulSandToSoil();

        registerDropLostSouls();

        registerTrimTallBush();
        registerTrimOrnamentalBush();
        registerTrimBush();
    }

    private static void registerSoulSoilTilling() {
        // Make soul soil tillable
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient) return ActionResult.PASS;

            ItemStack stack = player.getStackInHand(hand);
            Item item = stack.getItem();

            if (item instanceof HoeItem) {
                BlockPos pos = hitResult.getBlockPos();
                BlockState state = world.getBlockState(pos);

                if (state.isOf(Blocks.SOUL_SOIL)) {
                    world.setBlockState(pos, ModBlocks.SOUL_FARMLAND.getDefaultState(), Block.NOTIFY_ALL);

                    if (!player.isCreative()) stack.damage(1, player, LivingEntity.getSlotForHand(hand));

                    world.playSound(null, pos, SoundEvents.ITEM_HOE_TILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    return ActionResult.SUCCESS;
                }
            }

            return ActionResult.PASS;
        });
    }

    private static void registerSoulSandToSoil() {
        // Make soul soil tillable
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient) return ActionResult.PASS;

            ItemStack stack = player.getStackInHand(hand);
            Item item = stack.getItem();

            if (item instanceof HoeItem) {
                BlockPos pos = hitResult.getBlockPos();
                BlockState state = world.getBlockState(pos);

                if (state.isOf(Blocks.SOUL_SAND)) {
                    world.setBlockState(pos, Blocks.SOUL_SOIL.getDefaultState(), Block.NOTIFY_ALL);

                    if (!player.isCreative()) stack.damage(1, player, LivingEntity.getSlotForHand(hand));

                    world.playSound(null, pos, SoundEvents.ITEM_HOE_TILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    return ActionResult.SUCCESS;
                }
            }

            return ActionResult.PASS;
        });
    }

    private static void registerTrimBush() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient) return ActionResult.PASS;

            ItemStack stack = player.getStackInHand(hand);
            Item item = stack.getItem();

            if (item instanceof ShearsItem) {
                BlockPos pos = hitResult.getBlockPos();
                BlockState state = world.getBlockState(pos);

                if (state.isOf(Blocks.BUSH)) {
                    world.setBlockState(pos, Blocks.DEAD_BUSH.getDefaultState(), Block.NOTIFY_ALL);

                    if (!player.isCreative()) stack.damage(1, player, LivingEntity.getSlotForHand(hand));

                    world.playSound(null, pos, SoundEvents.ITEM_SHEARS_SNIP, SoundCategory.PLAYERS, 1.0F, 1.0F);
                    return ActionResult.SUCCESS;
                }
            }

            return ActionResult.PASS;
        });
    }

    private static void registerTrimTallBush() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient) return ActionResult.PASS;

            ItemStack stack = player.getStackInHand(hand);
            Item item = stack.getItem();

            if (item instanceof ShearsItem) {
                BlockPos pos = hitResult.getBlockPos();
                BlockState state = world.getBlockState(pos);

                if (state.isOf(ModBlocks.TALL_BUSH)) {
                    pos = (world.getBlockState(pos.down()).isOf(ModBlocks.TALL_BUSH)) ? pos.down() : pos;
                    world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
                    world.setBlockState(pos.up(), Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
                    TallPlantBlock.placeAt(world, ModBlocks.ORNAMENTAL_BUSH.getDefaultState(), pos, 3);

                    if (!player.isCreative()) stack.damage(1, player, LivingEntity.getSlotForHand(hand));

                    world.playSound(null, pos, SoundEvents.ITEM_SHEARS_SNIP, SoundCategory.PLAYERS, 1.0F, 1.0F);
                    return ActionResult.SUCCESS;
                }
            }

            return ActionResult.PASS;
        });
    }

    private static void registerTrimOrnamentalBush() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient) return ActionResult.PASS;

            ItemStack stack = player.getStackInHand(hand);
            Item item = stack.getItem();

            if (item instanceof ShearsItem) {
                BlockPos pos = hitResult.getBlockPos();
                BlockState state = world.getBlockState(pos);

                if (state.isOf(ModBlocks.ORNAMENTAL_BUSH)) {
                    pos = (world.getBlockState(pos.down()).isOf(ModBlocks.ORNAMENTAL_BUSH)) ? pos.down() : pos;
                    world.setBlockState(pos, Blocks.DEAD_BUSH.getDefaultState(), Block.NOTIFY_ALL);

                    if (!player.isCreative()) stack.damage(1, player, LivingEntity.getSlotForHand(hand));

                    world.playSound(null, pos, SoundEvents.ITEM_SHEARS_SNIP, SoundCategory.PLAYERS, 1.0F, 1.0F);
                    return ActionResult.SUCCESS;
                }
            }

            return ActionResult.PASS;
        });
    }

    private static void registerDropLostSouls() {
        ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register((world, entity, killedEntity) -> {
            if (world instanceof ServerWorld serverWorld
                    && ((ThreadLocalRandom.current().nextInt(100) + 1 < ConfigManager.CONFIG.lost_soul_drop_chance) && (killedEntity.getType().isIn(CropCritters.HAS_LOST_SOUL)))
                    && (entity instanceof PlayerEntity playerEntity)) {
                ItemStack stack = playerEntity.getMainHandStack();
                Item item = stack.getItem();
                if (item instanceof ShearsItem || item instanceof HoeItem) {
                    Vec3d pos = killedEntity.getPos();
                    // TODO Play sfx and spawn particles
                    ItemEntity ls = new ItemEntity(world, pos.x, pos.y, pos.z, new ItemStack(ModItems.LOST_SOUL));
                    serverWorld.spawnEntity(ls);
                }
            }
        });
    }
}
