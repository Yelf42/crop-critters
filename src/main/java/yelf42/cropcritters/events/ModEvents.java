package yelf42.cropcritters.events;

import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.*;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import yelf42.cropcritters.CropCritters;
import yelf42.cropcritters.blocks.*;
import yelf42.cropcritters.config.ConfigManager;
import yelf42.cropcritters.items.ModItems;

import java.util.Optional;

public class ModEvents {
    public static void initialize() {
        CropCritters.LOGGER.info("Initializing events for " + CropCritters.MOD_ID);
        registerSoulSoilTilling();
        registerSoulSandToSoil();

        registerSporesOnCrops();
        registerSnipStrangleFern();

        registerHarvestPopperPod();

        registerDropLostSouls();

        registerTrimTallBush();
        registerTrimOrnamentalBush();
        registerTrimBush();
    }

    private static void registerSoulSoilTilling() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient()) return ActionResult.PASS;

            ItemStack stack = player.getStackInHand(hand);

            if (stack.isIn(ItemTags.HOES)) {
                BlockPos pos = hitResult.getBlockPos();
                BlockState state = world.getBlockState(pos);

                if (state.isOf(Blocks.SOUL_SOIL)) {
                    world.setBlockState(pos, ModBlocks.SOUL_FARMLAND.getDefaultState(), Block.NOTIFY_ALL);

                    if (!player.isCreative()) stack.damage(1, player);

                    world.playSound(null, pos, SoundEvents.ITEM_HOE_TILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    return ActionResult.SUCCESS;
                }
            }

            return ActionResult.PASS;
        });
    }

    private static void registerSoulSandToSoil() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient()) return ActionResult.PASS;

            ItemStack stack = player.getStackInHand(hand);

            if (stack.isIn(ItemTags.HOES)) {
                BlockPos pos = hitResult.getBlockPos();
                BlockState state = world.getBlockState(pos);

                if (state.isOf(Blocks.SOUL_SAND)) {
                    world.setBlockState(pos, Blocks.SOUL_SOIL.getDefaultState(), Block.NOTIFY_ALL);

                    if (!player.isCreative()) stack.damage(1, player);

                    world.playSound(null, pos, SoundEvents.ITEM_HOE_TILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    return ActionResult.SUCCESS;
                }
            }

            return ActionResult.PASS;
        });
    }

    private static void registerSporesOnCrops() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient()) return ActionResult.PASS;

            ItemStack stack = player.getStackInHand(hand);

            if (stack.isOf(ModBlocks.STRANGLE_FERN.asItem())) {
                BlockState toPlant = ModBlocks.STRANGLE_FERN.getDefaultState();
                BlockPos pos = hitResult.getBlockPos();
                BlockState state = world.getBlockState(pos);

                if (toPlant.canPlaceAt(world, pos) && StrangleFern.canInfest(state)) {
                    world.setBlockState(pos, toPlant);

                    stack.decrementUnlessCreative(1, player);

                    world.playSound(null, pos, SoundEvents.BLOCK_SWEET_BERRY_BUSH_PLACE, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    return ActionResult.SUCCESS;
                }
            }

            return ActionResult.PASS;
        });
    }

    private static void registerSnipStrangleFern() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient()) return ActionResult.PASS;

            ItemStack stack = player.getStackInHand(hand);

            if (stack.isOf(Items.SHEARS)) {
                BlockPos pos = hitResult.getBlockPos();
                BlockState state = world.getBlockState(pos);

                if (state.isOf(ModBlocks.STRANGLE_FERN)) {
                    StrangleFernBlockEntity sfbe = (StrangleFernBlockEntity) world.getBlockEntity(pos);
                    BlockState infested = Blocks.DEAD_BUSH.getDefaultState();
                    if (sfbe != null) {
                        infested = sfbe.getInfestedState();
                    }
                    world.setBlockState(pos, infested, Block.NOTIFY_ALL);

                    if (!player.isCreative()) stack.damage(1, player);

                    world.playSound(null, pos, SoundEvents.ITEM_SHEARS_SNIP, SoundCategory.PLAYERS, 1.0F, 1.0F);
                    return ActionResult.SUCCESS;
                }
            }

            return ActionResult.PASS;
        });
    }

    private static void registerHarvestPopperPod() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient()) return ActionResult.PASS;

            ItemStack stack = player.getStackInHand(hand);

            if (stack.isOf(Items.SHEARS)) {
                BlockPos pos = hitResult.getBlockPos();
                BlockState state = world.getBlockState(pos);

                if (state.isOf(ModBlocks.POPPER_PLANT) && state.get(PopperPlantBlock.AGE, 0) == PopperPlantBlock.MAX_AGE) {
                    Vec3d center = pos.toCenterPos();
                    ItemStack itemStack = new ItemStack(ModItems.POPPER_POD);
                    ItemEntity itemEntity = new ItemEntity(world, center.x, center.y, center.z, itemStack);
                    itemEntity.setToDefaultPickupDelay();
                    world.spawnEntity(itemEntity);

                    world.setBlockState(pos, state.with(PopperPlantBlock.AGE, 0));

                    if (!player.isCreative()) stack.damage(1, player);

                    world.playSound(null, pos, SoundEvents.ITEM_SHEARS_SNIP, SoundCategory.PLAYERS, 1.0F, 1.0F);
                    return ActionResult.SUCCESS;
                }
            }

            return ActionResult.PASS;
        });
    }

    private static void registerTrimBush() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient()) return ActionResult.PASS;

            ItemStack stack = player.getStackInHand(hand);

            if (stack.isOf(Items.SHEARS)) {
                BlockPos pos = hitResult.getBlockPos();
                BlockState state = world.getBlockState(pos);

                if (state.isOf(Blocks.BUSH)) {
                    world.setBlockState(pos, Blocks.DEAD_BUSH.getDefaultState(), Block.NOTIFY_ALL);

                    if (!player.isCreative()) stack.damage(1, player);

                    world.playSound(null, pos, SoundEvents.ITEM_SHEARS_SNIP, SoundCategory.PLAYERS, 1.0F, 1.0F);
                    return ActionResult.SUCCESS;
                }
            }

            return ActionResult.PASS;
        });
    }

    private static void registerTrimTallBush() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient()) return ActionResult.PASS;

            ItemStack stack = player.getStackInHand(hand);

            if (stack.isOf(Items.SHEARS)) {
                BlockPos pos = hitResult.getBlockPos();
                BlockState state = world.getBlockState(pos);

                if (state.isOf(ModBlocks.TALL_BUSH)) {
                    pos = (world.getBlockState(pos.down()).isOf(ModBlocks.TALL_BUSH)) ? pos.down() : pos;
                    world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
                    world.setBlockState(pos.up(), Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
                    TallPlantBlock.placeAt(world, ModBlocks.ORNAMENTAL_BUSH.getDefaultState(), pos, 3);

                    if (!player.isCreative()) stack.damage(1, player);

                    world.playSound(null, pos, SoundEvents.ITEM_SHEARS_SNIP, SoundCategory.PLAYERS, 1.0F, 1.0F);
                    return ActionResult.SUCCESS;
                }
            }

            return ActionResult.PASS;
        });
    }

    private static void registerTrimOrnamentalBush() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient()) return ActionResult.PASS;

            ItemStack stack = player.getStackInHand(hand);

            if (stack.isOf(Items.SHEARS)) {
                BlockPos pos = hitResult.getBlockPos();
                BlockState state = world.getBlockState(pos);

                if (state.isOf(ModBlocks.ORNAMENTAL_BUSH)) {
                    pos = (world.getBlockState(pos.down()).isOf(ModBlocks.ORNAMENTAL_BUSH)) ? pos.down() : pos;
                    world.setBlockState(pos, Blocks.DEAD_BUSH.getDefaultState(), Block.NOTIFY_ALL);

                    if (!player.isCreative()) stack.damage(1, player);

                    world.playSound(null, pos, SoundEvents.ITEM_SHEARS_SNIP, SoundCategory.PLAYERS, 1.0F, 1.0F);
                    return ActionResult.SUCCESS;
                }
            }

            return ActionResult.PASS;
        });
    }

    // Lost soul drops for undead and critters on kill with Hoes
    private static void registerDropLostSouls() {
        ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register((world, entity, killedEntity, damageSource) -> {
            if (world instanceof ServerWorld serverWorld
                    && (killedEntity.getType().isIn(CropCritters.HAS_LOST_SOUL))
                    && (entity instanceof PlayerEntity playerEntity)) {
                ItemStack stack = playerEntity.getMainHandStack();
                int dropChance = ConfigManager.CONFIG.lostSoulDropChance;

                // Hoe + Critter?
                if (stack.isIn(ItemTags.HOES)) {
                    dropChance += ConfigManager.CONFIG.lostSoulDropChance;
                    dropChance += (killedEntity.getType().isIn(CropCritters.CROP_CRITTERS)) ? ConfigManager.CONFIG.lostSoulDropChance : 0;
                }

                // Silk Touch?
                Optional<RegistryEntry.Reference<Enchantment>> e = world.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT).getEntry(Enchantments.SILK_TOUCH.getValue());
                if (e.isPresent()) {
                    dropChance += (EnchantmentHelper.getLevel(e.get(), stack) > 0) ? 2 * ConfigManager.CONFIG.lostSoulDropChance : 0;
                }

                // Looting?
                Optional<RegistryEntry.Reference<Enchantment>> e2 = world.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT).getEntry(Enchantments.LOOTING.getValue());
                if (e2.isPresent()) {
                    dropChance += (ConfigManager.CONFIG.lostSoulDropChance / 2) * EnchantmentHelper.getLevel(e2.get(), stack);
                }

                if (serverWorld.random.nextInt(100) + 1 < dropChance) {
                    Vec3d pos = killedEntity.getEntityPos();
                    ItemEntity ls = new ItemEntity(world, pos.x, pos.y, pos.z, new ItemStack(ModItems.LOST_SOUL));
                    ls.setToDefaultPickupDelay();
                    serverWorld.spawnEntity(ls);
                }
            }
        });
    }
}
