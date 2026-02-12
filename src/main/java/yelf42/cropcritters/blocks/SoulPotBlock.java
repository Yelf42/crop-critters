package yelf42.cropcritters.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.tick.ScheduledTickView;
import org.jspecify.annotations.Nullable;
import yelf42.cropcritters.CropCritters;
import yelf42.cropcritters.items.ModItems;
import yelf42.cropcritters.particle.ModParticles;
import yelf42.cropcritters.sound.ModSounds;

public class SoulPotBlock extends BlockWithEntity implements Waterloggable {
    public static final MapCodec<SoulPotBlock> CODEC = createCodec(SoulPotBlock::new);
    public static final EnumProperty<Direction> FACING;
    public static final BooleanProperty CRACKED;
    public static final BooleanProperty WATERLOGGED;
    private static final VoxelShape SHAPE;

    public MapCodec<SoulPotBlock> getCodec() {
        return CODEC;
    }

    public SoulPotBlock(AbstractBlock.Settings settings) {
        super(settings);
        this.setDefaultState((((this.stateManager.getDefaultState()).with(FACING, Direction.NORTH)).with(WATERLOGGED, false)).with(CRACKED, false));
    }

    protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
        if (state.get(WATERLOGGED)) {
            tickView.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }

        return super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);
    }

    public BlockState getPlacementState(ItemPlacementContext ctx) {
        FluidState fluidState = ctx.getWorld().getFluidState(ctx.getBlockPos());
        return ((this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing())).with(WATERLOGGED, fluidState.getFluid() == Fluids.WATER)).with(CRACKED, false);
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (world.getTime() % 3L == 0L && world.getBlockState(pos.up()).isOf(Blocks.POTTED_WITHER_ROSE)) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof SoulPotBlockEntity soulPotBlockEntity && soulPotBlockEntity.getStack().getCount() > 0) {
                world.addParticleClient(ParticleTypes.SOUL_FIRE_FLAME,
                        pos.getX() + 0.5,
                        pos.getY() + 1.4,
                        pos.getZ() + 0.5,
                        0.0, 0.05, 0.0);
            }
        }
    }

    @Override
    protected boolean hasRandomTicks(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        BlockState above = world.getBlockState(pos.up());
        if (above.isOf(Blocks.POTTED_WITHER_ROSE)) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof SoulPotBlockEntity soulPotBlockEntity) {
                ItemStack inv = soulPotBlockEntity.getStack();
                if (inv != null && inv.isOf(ModItems.LOST_SOUL) && inv.getCount() >= 24) {
                    soulPotBlockEntity.setStack(inv.copyWithCount(inv.getCount() - 24));
                    world.setBlockState(pos.up(), ModBlocks.POTTED_SOUL_ROSE.getDefaultState());

                    world.playSound(null, pos.up(), ModSounds.WITHER_ROSE_CONVERT, SoundCategory.BLOCKS);
                    world.playSound(null, pos.up(), ModSounds.WITHER_ROSE_CONVERT_EXTRA, SoundCategory.BLOCKS);

                    Vec3d center = pos.up().toCenterPos();
                    CropCritters.ParticleRingS2CPayload payload = new CropCritters.ParticleRingS2CPayload(center, 0.5F, 10, ModParticles.SOUL_GLOW);
                    CustomPayloadS2CPacket packet = new CustomPayloadS2CPacket(payload);

                    for (ServerPlayerEntity player : world.getPlayers()) {
                        if (center.isInRange(player.getEntityPos(), 64)) {
                            player.networkHandler.sendPacket(packet);
                        }
                    }
                }
            }
        }
    }



    protected ActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof SoulPotBlockEntity soulPotBlockEntity) {
            if (world.isClient() || !stack.isOf(ModItems.LOST_SOUL)) {
                return ActionResult.SUCCESS;
            } else {
                ItemStack itemStack = soulPotBlockEntity.getStack();
                if (!stack.isEmpty() && (itemStack.isEmpty() || ItemStack.areItemsAndComponentsEqual(itemStack, stack) && itemStack.getCount() < itemStack.getMaxCount())) {
                    soulPotBlockEntity.wobble(SoulPotBlockEntity.WobbleType.POSITIVE);
                    player.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));
                    ItemStack itemStack2 = stack.splitUnlessCreative(1, player);
                    float f;
                    if (soulPotBlockEntity.isEmpty()) {
                        soulPotBlockEntity.setStack(itemStack2);
                        f = (float)itemStack2.getCount() / (float)itemStack2.getMaxCount();
                    } else {
                        soulPotBlockEntity.increaseStack();
                        f = (float)itemStack.getCount() / (float)itemStack.getMaxCount();
                    }

                    world.playSound(null, pos, SoundEvents.BLOCK_DECORATED_POT_INSERT, SoundCategory.BLOCKS, 1.0F, 0.7F + 0.5F * f);
                    if (world instanceof ServerWorld serverWorld) {
                        serverWorld.spawnParticles(ModParticles.SOUL_GLINT_PLUME, (double)pos.getX() + (double)0.5F, (double)pos.getY() + 1.2, (double)pos.getZ() + (double)0.5F, 5, 0.0F, 0.0F, 0.0F, 0.0F);
                    }

                    soulPotBlockEntity.markDirty();
                    world.emitGameEvent(player, GameEvent.BLOCK_CHANGE, pos);
                    return ActionResult.SUCCESS;
                } else {
                    return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
                }
            }
        } else {
            return ActionResult.PASS;
        }
    }

    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        BlockEntity var7 = world.getBlockEntity(pos);
        if (var7 instanceof SoulPotBlockEntity soulPotBlockEntity) {
            world.playSound(null, pos, SoundEvents.BLOCK_DECORATED_POT_INSERT_FAIL, SoundCategory.BLOCKS, 1.0F, 1.0F);
            soulPotBlockEntity.wobble(SoulPotBlockEntity.WobbleType.NEGATIVE);
            world.emitGameEvent(player, GameEvent.BLOCK_CHANGE, pos);
            return ActionResult.SUCCESS;
        } else {
            return ActionResult.PASS;
        }
    }

    protected boolean canPathfindThrough(BlockState state, NavigationType type) {
        return false;
    }

    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED, CRACKED);
    }

    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new SoulPotBlockEntity(pos, state);
    }

    protected void onStateReplaced(BlockState state, ServerWorld world, BlockPos pos, boolean moved) {
        ItemScatterer.onStateReplaced(state, world, pos);
    }

    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        ItemStack itemStack = player.getMainHandStack();
        BlockState blockState = state;
        if (itemStack.isIn(ItemTags.BREAKS_DECORATED_POTS) && !EnchantmentHelper.hasAnyEnchantmentsIn(itemStack, EnchantmentTags.PREVENTS_DECORATED_POT_SHATTERING)) {
            blockState = state.with(CRACKED, true);
            world.setBlockState(pos, blockState, 260);
        }

        return super.onBreak(world, pos, blockState, player);
    }

    protected FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    protected BlockSoundGroup getSoundGroup(BlockState state) {
        return state.get(CRACKED) ? BlockSoundGroup.DECORATED_POT_SHATTER : BlockSoundGroup.DECORATED_POT;
    }

    protected void onProjectileHit(World world, BlockState state, BlockHitResult hit, ProjectileEntity projectile) {
        BlockPos blockPos = hit.getBlockPos();
        if (world instanceof ServerWorld serverWorld) {
            if (projectile.canModifyAt(serverWorld, blockPos) && projectile.canBreakBlocks(serverWorld)) {
                world.setBlockState(blockPos, state.with(CRACKED, true), 260);
                world.breakBlock(blockPos, true, projectile);
            }
        }

    }

    protected boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    protected int getComparatorOutput(BlockState state, World world, BlockPos pos, Direction direction) {
        return ScreenHandler.calculateComparatorOutput(world.getBlockEntity(pos));
    }

    protected BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    protected BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    static {
        FACING = Properties.HORIZONTAL_FACING;
        CRACKED = Properties.CRACKED;
        WATERLOGGED = Properties.WATERLOGGED;
        SHAPE = Block.createColumnShape(14.0F, 0.0F, 16.0F);
    }
}
