package yelf42.cropcritters.items;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ProjectileItem;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Position;
import net.minecraft.world.World;
import yelf42.cropcritters.entity.SeedBallProjectileEntity;
import yelf42.cropcritters.sound.ModSounds;

public class SeedBallItem extends Item implements ProjectileItem {
    public SeedBallItem(Item.Settings settings) {
        super(settings);
    }

    @Override
    public ProjectileEntity createEntity(World world, Position pos, ItemStack stack, Direction direction) {
        return new SeedBallProjectileEntity(pos.getX(), pos.getY(), pos.getZ(), world, stack);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        world.playSound(null, user.getX(), user.getY(), user.getZ(), ModSounds.THROW_SEED_BALL, SoundCategory.NEUTRAL, 0.5F, 0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F));
        if (world instanceof ServerWorld serverWorld) {
            ProjectileEntity.spawnWithVelocity(SeedBallProjectileEntity::new, serverWorld, itemStack, user, 0.0F, 1.5F, 1.0F);
        }

        itemStack.decrementUnlessCreative(1, user);
        return ActionResult.SUCCESS;
    }
}
