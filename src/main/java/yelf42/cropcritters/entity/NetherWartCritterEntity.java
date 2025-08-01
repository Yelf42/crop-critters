package yelf42.cropcritters.entity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import yelf42.cropcritters.blocks.ModBlocks;

import java.util.function.Predicate;

// TODO override isShaking() in the Render class

public class NetherWartCritterEntity extends AbstractCropCritterEntity {

    private final int GO_CRAZY = 400;
    int lifespan;

    public NetherWartCritterEntity(EntityType<? extends TameableEntity> entityType, World world) {
        super(entityType, world);
        this.lifespan = 1200;
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.MAX_HEALTH, 6)
                .add(EntityAttributes.MOVEMENT_SPEED, 0.25)
                .add(EntityAttributes.ATTACK_DAMAGE, 1)
                .add(EntityAttributes.FOLLOW_RANGE, 10)
                .add(EntityAttributes.TEMPT_RANGE, 10);
    }

    @Override
    protected Predicate<BlockState> getTargetBlockFilter() {
        return (blockState -> blockState.isOf(Blocks.SOUL_SOIL)
                                        || blockState.isOf((Blocks.SOUL_SAND))
                                        || blockState.isOf(ModBlocks.SOUL_FARMLAND)
                                        || blockState.isOf(Blocks.BLACKSTONE));
    }

    @Override
    protected  int getTargetOffset() {return 1;}

    @Override
    public void completeTargetGoal() {
        if (this.targetPos == null) return;
        BlockState target = this.getWorld().getBlockState(this.targetPos);
        if (target.isOf(Blocks.SOUL_SAND) || target.isOf(Blocks.SOUL_SOIL)) {
            this.playSound(SoundEvents.BLOCK_SOUL_SAND_PLACE, 1.0F, 1.0F);
            this.getWorld().setBlockState(this.targetPos.up(), Blocks.NETHER_WART.getDefaultState(), Block.NOTIFY_ALL_AND_REDRAW);
            ((ServerWorld)this.getWorld()).spawnParticles(ParticleTypes.HAPPY_VILLAGER, this.targetPos.getX() + 0.5, this.targetPos.getY() + 1.0, this.targetPos.getZ() + 0.5, 10, 0.5, 0.5, 0.5, 0.0);
            this.discard();
        } else {
            explode();
        }
    }

    @Override
    protected boolean isHealingItem(ItemStack itemStack) {
        return itemStack.isOf(Items.NETHER_WART);
    }

    @Override
    protected int resetTicksUntilCanWork() {
        return 10;
    }

    @Override
    protected boolean canWork() {
        return !this.isTrusting();
    }

    @Override
    public boolean isShaking() {
        return this.lifespan < this.GO_CRAZY;
    }

    @Override
    protected void tryTame(PlayerEntity player) {
        if (this.lifespan >= this.GO_CRAZY) super.tryTame(null);
    }

    @Override
    protected void mobTick(ServerWorld world) {
        super.mobTick(world);
        if (!this.isTrusting()) lifespan--;
        if (lifespan <= 0) explode();
    }

    private void explode() {
        if (this.getWorld() instanceof ServerWorld serverWorld) {
            // TODO SFX and particles
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    for (int k = -1; k <= 1; k++) {
                        BlockPos pos = this.getBlockPos().add(i, j, k);
                        if (serverWorld.getBlockState(pos).isOf(Blocks.BLACKSTONE)) {
                            serverWorld.setBlockState(pos, Blocks.SOUL_SAND.getDefaultState(), Block.NOTIFY_LISTENERS);
                        } else if (serverWorld.getBlockState(pos).isOf(ModBlocks.WITHERING_SPITEWEED)) {
                            serverWorld.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
                        }
                    }
                }
            }
            this.dead = true;
            this.onRemoval(serverWorld, RemovalReason.KILLED);
            this.discard();
        }
    }
}
