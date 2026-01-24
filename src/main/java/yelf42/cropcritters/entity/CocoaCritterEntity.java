package yelf42.cropcritters.entity;

import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootWorldContext;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.object.PlayState;
import software.bernie.geckolib.constant.DefaultAnimations;
import yelf42.cropcritters.CropCritters;
import yelf42.cropcritters.config.RecognizedCropsState;
import yelf42.cropcritters.items.ModItems;

import java.util.*;
import java.util.function.Predicate;

public class CocoaCritterEntity extends AbstractCropCritterEntity {

    private static final Predicate<Entity> NOTICEABLE_PLAYER_FILTER = (entity) -> !entity.isSneaky() && EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR.test(entity);
    private static final Predicate<ItemEntity> PICKABLE_DROP_FILTER = (item) -> !item.cannotPickup() && item.isAlive();
    public static final RawAnimation HOLD = RawAnimation.begin().thenPlayAndHold("holding");
    private static final Set<Item> DEFAULT_KNOWN_ITEMS = new HashSet<>();

    private static final int MAX_HOPPER_DISTANCE = 32;

    @Nullable
    BlockPos hopperPos;

    static {
        DEFAULT_KNOWN_ITEMS.add(ModItems.STRANGE_FERTILIZER);
        DEFAULT_KNOWN_ITEMS.add(ModItems.LOST_SOUL);
        DEFAULT_KNOWN_ITEMS.add(Items.COCOA_BEANS);
        DEFAULT_KNOWN_ITEMS.add(Items.WHEAT_SEEDS);
        DEFAULT_KNOWN_ITEMS.add(Items.WHEAT);
        DEFAULT_KNOWN_ITEMS.add(Items.CARROT);
        DEFAULT_KNOWN_ITEMS.add(Items.POTATO);
        DEFAULT_KNOWN_ITEMS.add(Items.POISONOUS_POTATO);
        DEFAULT_KNOWN_ITEMS.add(Items.MELON_SLICE);
        DEFAULT_KNOWN_ITEMS.add(Items.MELON_SEEDS);
        DEFAULT_KNOWN_ITEMS.add(Items.PUMPKIN_SEEDS);
        DEFAULT_KNOWN_ITEMS.add(Items.TORCHFLOWER);
        DEFAULT_KNOWN_ITEMS.add(Items.TORCHFLOWER_SEEDS);
        DEFAULT_KNOWN_ITEMS.add(Items.PITCHER_PLANT);
        DEFAULT_KNOWN_ITEMS.add(Items.PITCHER_POD);
    }

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        view.putNullable("hopper_pos", BlockPos.CODEC, this.hopperPos);
    }

    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view);
        this.hopperPos = (BlockPos)view.read("hopper_pos", BlockPos.CODEC).orElse(null);
    }


    public CocoaCritterEntity(EntityType<? extends TameableEntity> entityType, World world) {
        super(entityType, world);
        this.setCanPickUpLoot(true);
    }

    protected void initGoals() {
        net.minecraft.entity.ai.goal.TemptGoal temptGoal = new TemptGoal(this, 0.6, (stack) -> stack.isOf(ModItems.LOST_SOUL), true);
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(2, temptGoal);
        this.goalSelector.add(6, new FleeEntityGoal<>(this, PlayerEntity.class, 10.0F, 1.6, 1.4, (entity) -> NOTICEABLE_PLAYER_FILTER.test(entity) && !this.isTrusting()));
        this.goalSelector.add(7, new DepositInHopperGoal());
        this.targetWorkGoal = new TargetWorkGoal();
        this.goalSelector.add(8, this.targetWorkGoal);
        this.goalSelector.add(9, new PickupItemGoal());
        this.goalSelector.add(12, new WanderAroundGoal(this, 0.8));
        this.goalSelector.add(20, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
        this.goalSelector.add(20, new LookAroundGoal(this));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(
                DefaultAnimations.genericWalkIdleController(),
                new AnimationController<>("Hold", test -> {
                    if ((this.getEquippedStack(EquipmentSlot.MAINHAND).isEmpty())) {
                        test.controller().reset();
                        return PlayState.STOP;
                    } else {
                        return (test.setAndContinue(HOLD));
                    }
                    //return this.getEquippedStack(EquipmentSlot.MAINHAND).isEmpty() ? PlayState.STOP : (test.setAndContinue(HOLD));
                })
        );
    }

    @Override
    protected Predicate<BlockState> getTargetBlockFilter() {
        return (blockState -> ((blockState.getBlock() instanceof CropBlock cropBlock && cropBlock.isMature(blockState)))
                || (blockState.getBlock() instanceof NetherWartBlock && blockState.get(NetherWartBlock.AGE, 0) >= NetherWartBlock.MAX_AGE));
    }

    @Override
    protected int getTargetOffset() {
        return 0;
    }

    @Override
    protected boolean canWork() {
        return this.getEquippedStack(EquipmentSlot.MAINHAND).isEmpty();
    }

    @Override
    protected Pair<Item, Integer> getLoot() {
        return new Pair<>(Items.COCOA_BEANS, 3);
    }

    @Override
    protected boolean isHealingItem(ItemStack itemStack) {
        return itemStack.isOf(Items.COCOA_BEANS);
    }

    @Override
    protected int resetTicksUntilCanWork() {
        return resetTicksUntilCanWork(MathHelper.nextInt(this.random, 500, 600));
    }


    @Override
    public void completeTargetGoal() {
        if (!this.getEquippedStack(EquipmentSlot.MAINHAND).isEmpty() || this.targetPos == null || this.getEntityWorld().isClient()) return;
        ServerWorld world = (ServerWorld) this.getEntityWorld();
        BlockState state = world.getBlockState(this.targetPos);
        if (!getTargetBlockFilter().test(state)) return;

        LootWorldContext.Builder builder = (new LootWorldContext.Builder(world)).add(LootContextParameters.ORIGIN, Vec3d.ofCenter(this.targetPos)).add(LootContextParameters.TOOL, ItemStack.EMPTY);
        List<ItemStack> items = state.getDroppedStacks(builder);
        if (items.isEmpty()) return;

        ItemStack toDrop = items.get(world.random.nextInt(items.size()));
        toDrop.setCount(1);
        this.equipStack(EquipmentSlot.MAINHAND, toDrop);
        this.setDropGuaranteed(EquipmentSlot.MAINHAND);
        recordCrop(toDrop.getItem());

        world.syncWorldEvent(this, 2001, this.targetPos, Block.getRawIdFromState(state));
        world.setBlockState(this.targetPos, Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
    }

    private void recordCrop(Item item) {
        if (this.getEntityWorld().isClient()) return;
        ServerWorld world = (ServerWorld)this.getEntityWorld();
        RecognizedCropsState state = RecognizedCropsState.getServerState(world.getServer());
        state.addCrop(item);
    }

    private boolean checkCrop(Item item) {
        if (this.getEntityWorld().isClient()) return false;
        if (DEFAULT_KNOWN_ITEMS.contains(item)) return true;
        ServerWorld world = (ServerWorld)this.getEntityWorld();
        RecognizedCropsState state = RecognizedCropsState.getServerState(world.getServer());
        return state.hasCrop(item);
    }

    @Override
    protected void drop(ServerWorld world, DamageSource damageSource) {
        ItemStack itemStack = this.getEquippedStack(EquipmentSlot.MAINHAND);
        if (!itemStack.isEmpty()) {
            this.dropStack(world, itemStack);
            this.equipStack(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        }

        super.drop(world, damageSource);
    }

    // Right click with empty hand makes critter try drop held item
    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ActionResult actionResult = super.interactMob(player, hand);
        if (!this.getEntityWorld().isClient() && !actionResult.isAccepted() && this.isTrusting() && player.getStackInHand(hand).isEmpty()) {
            if (tryPutDown(this.getEquippedStack(EquipmentSlot.MAINHAND), true)) return ActionResult.SUCCESS;
        }
        return actionResult;
    }

    // For dropping held item
    private boolean tryPutDown(ItemStack stack, boolean withVelocity) {
        if (!stack.isEmpty()) {
            ItemEntity itemEntity;
            if (withVelocity) {
                itemEntity = new ItemEntity(this.getEntityWorld(), this.getX() + this.getRotationVector().x, this.getY() + (double)0.6F, this.getZ() + this.getRotationVector().z, stack);
            } else {
                itemEntity = new ItemEntity(this.getEntityWorld(), this.getX(), this.getY() + (double)0.6F, this.getZ(), stack, 0F, 0F, 0F);
            }
            itemEntity.setPickupDelay(40);
            itemEntity.setThrower(this);
            this.playSound(SoundEvents.ENTITY_FOX_SPIT, 1.0F, 1.0F);
            this.getEntityWorld().spawnEntity(itemEntity);
            this.equipStack(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            return true;
        }
        return false;
    }

    @Override
    public boolean canPickupItem(ItemStack stack) {
        ItemStack itemStack = this.getEquippedStack(EquipmentSlot.MAINHAND);
        return itemStack.isEmpty() && checkCrop(stack.getItem());
    }

    private void dropItem(ItemStack stack) {
        ItemEntity itemEntity = new ItemEntity(this.getEntityWorld(), this.getX(), this.getY(), this.getZ(), stack);
        this.getEntityWorld().spawnEntity(itemEntity);
    }

    @Override
    protected void loot(ServerWorld world, ItemEntity itemEntity) {
        ItemStack itemStack = itemEntity.getStack();
        if (this.canPickupItem(itemStack)) {
            int i = itemStack.getCount();
            if (i > 1) {
                this.dropItem(itemStack.split(i - 1));
            }
            this.triggerItemPickedUpByEntityCriteria(itemEntity);
            this.equipStack(EquipmentSlot.MAINHAND, itemStack.split(1));
            this.setDropGuaranteed(EquipmentSlot.MAINHAND);
            this.sendPickup(itemEntity, itemStack.getCount());
            itemEntity.discard();
        }

    }

    protected boolean validHopperPos() {
        return this.hopperPos != null
                && this.getEntityWorld().getBlockState(this.hopperPos).isOf(Blocks.HOPPER)
                && this.hopperPos.isWithinDistance(this.getEntityPos(), MAX_HOPPER_DISTANCE);
    }

    class DepositInHopperGoal extends Goal {
        protected Long2LongOpenHashMap unreachableTargetsPosCache = new Long2LongOpenHashMap();
        protected int ticks;
        protected Vec3d nextTarget;

        @Override
        public boolean canStart() {
            if (!CocoaCritterEntity.this.isTrusting() || CocoaCritterEntity.this.getEquippedStack(EquipmentSlot.MAINHAND).isEmpty()) return false;
            if (validHopperPos()) return true;
            Optional<BlockPos> optional = this.getTargetBlock();
            if (optional.isPresent()) {
                CocoaCritterEntity.this.hopperPos = optional.get();
                return true;
            }
            return false;
        }

        @Override
        public void start() {
            this.ticks = 0;
        }

        @Override
        public boolean shouldContinue() {
            return validHopperPos() && !CocoaCritterEntity.this.getEquippedStack(EquipmentSlot.MAINHAND).isEmpty();
        }

        @Override
        public void tick() {
            ++this.ticks;
            if (this.ticks > 300 || !validHopperPos()) {
                CocoaCritterEntity.this.hopperPos = null;
            } else {
                // validHopperPos() checks for null, ignore warning
                Vec3d vec3d = Vec3d.ofBottomCenter(CocoaCritterEntity.this.hopperPos).add(0.0F, 1, 0.0F);
                if (vec3d.squaredDistanceTo(CocoaCritterEntity.this.getEntityPos()) > (double)1.0F) {
                    this.nextTarget = vec3d;
                    this.moveToNextTarget();
                } else {
                    if (this.nextTarget == null) {
                        this.nextTarget = vec3d;
                    }

                    boolean bl = CocoaCritterEntity.this.getEntityPos().distanceTo(this.nextTarget) <= 0.5;
                    if (!bl && this.ticks > 300) {
                        CocoaCritterEntity.this.hopperPos = null;
                    } else if (bl) {
                        ItemStack stack = CocoaCritterEntity.this.getEquippedStack(EquipmentSlot.MAINHAND);
                        CocoaCritterEntity.this.tryPutDown(stack, false);
                    } else {
                        CocoaCritterEntity.this.getMoveControl().moveTo(this.nextTarget.getX(), this.nextTarget.getY(), this.nextTarget.getZ(), 0.8F);
                    }
                }
            }
        }

        protected void moveToNextTarget() {
            CocoaCritterEntity.this.navigation.startMovingAlong(CocoaCritterEntity.this.navigation.findPathTo(this.nextTarget.getX(), this.nextTarget.getY(), this.nextTarget.getZ(), 0), 1.2F);
        }

        protected boolean checkHopper(BlockPos blockPos) {
            return (CocoaCritterEntity.this.getEntityWorld().getBlockState(blockPos).isOf(Blocks.HOPPER))
                    && (CocoaCritterEntity.this.getEntityWorld().getBlockState(blockPos.up()).canPathfindThrough(NavigationType.LAND));
        }

        protected Optional<BlockPos> getTargetBlock() {
            Iterable<BlockPos> iterable = BlockPos.iterateOutwards(CocoaCritterEntity.this.getBlockPos(), 12, 2, 12);
            Long2LongOpenHashMap long2LongOpenHashMap = new Long2LongOpenHashMap();

            for(BlockPos blockPos : iterable) {
                long l = this.unreachableTargetsPosCache.getOrDefault(blockPos.asLong(), Long.MIN_VALUE);
                if (CocoaCritterEntity.this.getEntityWorld().getTime() < l) {
                    long2LongOpenHashMap.put(blockPos.asLong(), l);
                } else if (checkHopper(blockPos)) {
                    Path path = CocoaCritterEntity.this.navigation.findPathTo(blockPos, 0);
                    if (path != null && path.reachesTarget()) {
                        return Optional.of(blockPos);
                    }
                    long2LongOpenHashMap.put(blockPos.asLong(), CocoaCritterEntity.this.getEntityWorld().getTime() + 600L);
                }
            }
            this.unreachableTargetsPosCache = long2LongOpenHashMap;
            return Optional.empty();
        }

    }

    class PickupItemGoal extends Goal {
        public PickupItemGoal() {
            this.setControls(EnumSet.of(Control.MOVE));
        }

        public boolean canStart() {
            if (!CocoaCritterEntity.this.getEquippedStack(EquipmentSlot.MAINHAND).isEmpty()) return false;
            if (CocoaCritterEntity.this.getRandom().nextInt(toGoalTicks(10)) != 0) {
                return false;
            } else {
                List<ItemEntity> list = CocoaCritterEntity.this.getEntityWorld().getEntitiesByClass(ItemEntity.class, CocoaCritterEntity.this.getBoundingBox().expand(8.0F, 8.0F, 8.0F), CocoaCritterEntity.PICKABLE_DROP_FILTER);
                return !list.isEmpty();
            }
        }

        public void tick() {
            List<ItemEntity> list = CocoaCritterEntity.this.getEntityWorld().getEntitiesByClass(ItemEntity.class, CocoaCritterEntity.this.getBoundingBox().expand(8.0F, 8.0F, 8.0F), CocoaCritterEntity.PICKABLE_DROP_FILTER);
            ItemStack itemStack = CocoaCritterEntity.this.getEquippedStack(EquipmentSlot.MAINHAND);
            if (itemStack.isEmpty() && !list.isEmpty()) {
                moveTowardsCropItem(list);
            }
        }

        public void start() {
            List<ItemEntity> list = CocoaCritterEntity.this.getEntityWorld().getEntitiesByClass(ItemEntity.class, CocoaCritterEntity.this.getBoundingBox().expand(8.0F, 8.0F, 8.0F), CocoaCritterEntity.PICKABLE_DROP_FILTER);
            if (!list.isEmpty()) {
                moveTowardsCropItem(list);
            }
        }

        private void moveTowardsCropItem(List<ItemEntity> list) {
            for (ItemEntity itemEntity : list) {
                if (CocoaCritterEntity.this.checkCrop(itemEntity.getStack().getItem())) {
                    CocoaCritterEntity.this.getNavigation().startMovingTo(itemEntity, 1.2F);
                    return;
                }
            }
        }
    }

}
