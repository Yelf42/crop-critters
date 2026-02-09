package yelf42.cropcritters.items;

import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.*;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import yelf42.cropcritters.config.CritterHelper;
import yelf42.cropcritters.entity.AbstractCropCritterEntity;
import yelf42.cropcritters.sound.ModSounds;

import java.util.Optional;

public class LostSoulItem extends Item {

    public LostSoulItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (context.getWorld().isClient()) return ActionResult.PASS;
        ServerWorld world = (ServerWorld) context.getWorld();
        BlockPos blockPos = context.getBlockPos();
        BlockState state = world.getBlockState(blockPos);
        ItemStack itemStack = context.getStack();
        PlayerEntity playerEntity = context.getPlayer();

        // Create slime
        if (state.isOf(Blocks.SLIME_BLOCK)) {
            if (world.random.nextInt(2) == 0) {
                world.spawnParticles(ParticleTypes.SMOKE, blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5, 10, 0.5, 0.5, 0.5, 0F);
            } else {
                world.setBlockState(blockPos, Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
                SlimeEntity slime = EntityType.SLIME.create(world, SpawnReason.SPAWN_ITEM_USE);
                slime.setSize(2, true);
                slime.setPosition(blockPos.toBottomCenterPos());
                world.spawnEntity(slime);
                world.playSound(null, blockPos, ModSounds.SPAWN_SLIME, SoundCategory.BLOCKS, 1F, 1F);
                world.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME, blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5, 10, 0.5, 0.5, 0.5, 0F);
            }
            if (playerEntity instanceof ServerPlayerEntity serverPlayerEntity) {
                Criteria.ITEM_USED_ON_BLOCK.trigger(serverPlayerEntity, blockPos, itemStack);
            }
            itemStack.decrementUnlessCreative(1, playerEntity);
            return ActionResult.SUCCESS;
        }

        // Critter spawning logic
        Optional<AbstractCropCritterEntity> toSpawn = CritterHelper.spawnCritterWithItem(world, state);
        if (toSpawn.isEmpty()) return ActionResult.PASS;
        AbstractCropCritterEntity critter = toSpawn.get();
        int failChance = (critter.getMaxHealth() > 12) ? 80 : 60;
        if (world.random.nextInt(100) + 1 <= failChance) {
            world.spawnParticles(ParticleTypes.SMOKE, blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5, 10, 0.5, 0.5, 0.5, 0F);
        } else {
            BlockPos toSpawnAt = blockPos;
            if (state.get(PitcherCropBlock.HALF, DoubleBlockHalf.LOWER) == DoubleBlockHalf.UPPER) {
                world.setBlockState(blockPos.down(), Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
                toSpawnAt = blockPos.down();
            }
            world.setBlockState(blockPos, Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
            critter.setPosition(toSpawnAt.toBottomCenterPos());
            world.spawnEntity(critter);
            world.playSound(null, blockPos, ModSounds.SPAWN_CRITTER, SoundCategory.BLOCKS, 1F, 1F);
            world.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME, blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5, 10, 0.5, 0.5, 0.5, 0F);
            if (playerEntity instanceof ServerPlayerEntity serverPlayerEntity) {
                Criteria.ITEM_USED_ON_BLOCK.trigger(serverPlayerEntity, blockPos, itemStack);
            }
            itemStack.decrementUnlessCreative(1, playerEntity);
        }
        return ActionResult.SUCCESS;
    }
}
