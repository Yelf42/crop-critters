package yelf42.cropcritters.entity;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.tick.TickPriority;
import yelf42.cropcritters.CropCritters;
import yelf42.cropcritters.blocks.ModBlocks;
import yelf42.cropcritters.items.ModItems;

import java.util.function.Predicate;

public class TorchflowerCritterEntity extends AbstractCropCritterEntity {
    private static final Predicate<Entity> NOTICEABLE_PLAYER_FILTER = (entity) -> !entity.isSneaky() && EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR.test(entity);
    private static final Predicate<Entity> FARM_ANIMALS_FILTER = (entity -> entity.getType().isIn(CropCritters.SCARE_CRITTERS));


    public TorchflowerCritterEntity(EntityType<? extends TameableEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected Predicate<BlockState> getTargetBlockFilter() {return null;}
    @Override
    protected int getTargetOffset() {return 0;}

    @Override
    protected Pair<Item, Integer> getLoot() {
        return new Pair<>(Items.TORCHFLOWER, 1);
    }

    @Override
    protected boolean isHealingItem(ItemStack itemStack) {return itemStack.isOf(Items.TORCHFLOWER);}

    @Override
    protected int resetTicksUntilCanWork() {return 0;}
    @Override
    public void completeTargetGoal() {}

    @Override
    protected void initGoals() {
        net.minecraft.entity.ai.goal.TemptGoal temptGoal = new TemptGoal(this, 0.6, (stack) -> stack.isOf(ModItems.LOST_SOUL), true);
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new SitGoal(this));
        this.goalSelector.add(2, temptGoal);
        this.goalSelector.add(6, new FollowOwnerGoal(this, 1.0F, 10.0F, 5.0F));
        this.goalSelector.add(4, new FleeEntityGoal<>(this, AnimalEntity.class, 10.0F, 1.6, 1.4, (entity) -> FARM_ANIMALS_FILTER.test(entity) && !this.isTrusting()));
        this.goalSelector.add(6, new FleeEntityGoal<>(this, PlayerEntity.class, 10.0F, 1.6, 1.4, (entity) -> NOTICEABLE_PLAYER_FILTER.test(entity) && !this.isTrusting()));
        this.goalSelector.add(12, new WanderAroundGoal(this, 0.8));
        this.goalSelector.add(20, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
        this.goalSelector.add(20, new LookAroundGoal(this));
    }

    @Override
    protected void mobTick(ServerWorld world) {
        super.mobTick(world);
        BlockPos pos = this.getBlockPos().up();
        if (this.isTrusting() && world.getBlockState(pos).isAir()) {
            world.setBlockState(pos, ModBlocks.TORCHFLOWER_SPARK.getDefaultState());
            world.scheduleBlockTick(pos, ModBlocks.TORCHFLOWER_SPARK, 200, TickPriority.EXTREMELY_LOW);
        }
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ActionResult actionResult = super.interactMob(player, hand);
        CropCritters.LOGGER.info("Owner? " + this.isOwner(player));
        if (!actionResult.isAccepted() && this.isTrusting() && this.isOwner(player)) {
            this.setSitting(!this.isSitting());
            return ActionResult.SUCCESS;
        }
        return actionResult;
    }

    @Override
    protected void tryTame(PlayerEntity player) {
        super.tryTame(player);
        if (this.isTamed()) this.setSitting(true);
    }
}
