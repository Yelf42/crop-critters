package yelf42.cropcritters.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeKeys;
import yelf42.cropcritters.area_affectors.AffectorType;
import yelf42.cropcritters.effects.ModEffects;
import yelf42.cropcritters.particle.ModParticles;

import java.util.List;
import java.util.function.Predicate;

public class SoulRoseBlockEntity extends BlockEntity {
    private static final Predicate<BlockState> CORE_MATERIALS = (blockState -> blockState.isOf(Blocks.RAW_COPPER_BLOCK) || blockState.isOf(Blocks.RAW_IRON_BLOCK) || blockState.isOf(Blocks.RAW_GOLD_BLOCK));

    // Central core
    public static final Vec3i[] STAGE_1A = new Vec3i[]{
            new Vec3i(0,-3,0),
            new Vec3i(0,-4,0),
            new Vec3i(0,-5,0)
    };
    public static final Vec3i[] STAGE_1B = new Vec3i[]{
            new Vec3i(1,-2,0),
            new Vec3i(2,-2,0),
            new Vec3i(3,-2,0),
    };

    public static final Vec3i[] STAGE_2 = new Vec3i[]{
            new Vec3i(4,-2,0),

            new Vec3i(4,-3,0),
            new Vec3i(4,-4,0),

            new Vec3i(4,-2,-1),
            new Vec3i(4,-2,-2),

            new Vec3i(4,-2,1),
            new Vec3i(4,-2,2),

            new Vec3i(5,-2,0),
            new Vec3i(6,-2,0),
            new Vec3i(7,-2,0)
    };

    public static final Vec3i[] STAGE_3 = new Vec3i[]{
            new Vec3i(8,-2,0),

            new Vec3i(8,-3,0),

            new Vec3i(8,-2,-1),

            new Vec3i(8,-2,1),

            new Vec3i(8,-2,0),
            new Vec3i(9,-2,0),
            new Vec3i(10,-2,0),
            new Vec3i(11,-2,0)
    };


    public SoulRoseBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SOUL_ROSE, pos, state);
    }

    // Particles
    public static void clientTick(World world, BlockPos pos, BlockState state, SoulRoseBlockEntity blockEntity) {
        int level = state.get(SoulRoseBlock.LEVEL, 0);
        SoulRoseType type = state.get(SoulRoseBlock.TYPE, SoulRoseType.NONE);
        if (level == 0 || type == SoulRoseType.NONE) return;

        // Particles for being active
        if (world.getTime() % 20 == 0L) {
            if (world.isDay()) return;
            if (world.getDimension().hasFixedTime() && !world.getBiome(pos).matchesKey(BiomeKeys.SOUL_SAND_VALLEY)) return;

            BlockPos.Mutable mutable = new BlockPos.Mutable();
            Random random = world.random;
            int i = pos.getX();
            int j = pos.getY();
            int k = pos.getZ();

            for(int l = 0; l < 2 * level; ++l) {
                double angle = random.nextDouble() * Math.PI * 2.0F;
                double radius = random.nextDouble() * levelToRadius(level) - 1.0F;
                mutable.set(i + radius * Math.sin(angle), j - random.nextInt(2), k + radius * Math.cos(angle));
                world.addParticleClient(ModParticles.SOUL_SIPHON, (double)mutable.getX() + random.nextDouble(), mutable.getY(), (double)mutable.getZ() + random.nextDouble(), (double)0.0F, (double)0.0F, (double)0.0F);
            }
        }

    }

    // Update Level + Copper Attack
    public static void serverTick(World world, BlockPos pos, BlockState state, SoulRoseBlockEntity blockEntity) {
        // Update block level and type
        if (world.getTime() % 100L == 0L) {
            int lvl = updateLevel(world, pos);
            if (lvl != state.get(SoulRoseBlock.LEVEL, 0)) {
                BlockState core = world.getBlockState(pos.add(0, -2, 0));
                world.setBlockState(pos, state.with(SoulRoseBlock.LEVEL, lvl).with(SoulRoseBlock.TYPE, SoulRoseType.getType(core, lvl)));
                if (SoulRoseBlock.isDoubleTallAtLevel(lvl)) {
                    world.setBlockState(pos.up(), state.with(SoulRoseBlock.LEVEL, lvl).with(SoulRoseBlock.TYPE, SoulRoseType.getType(core, lvl)).with(SoulRoseBlock.HALF, DoubleBlockHalf.UPPER));
                } else {
                    if (world.getBlockState(pos.up()).isOf(ModBlocks.SOUL_ROSE)) world.setBlockState(pos.up(), Blocks.AIR.getDefaultState());
                }
            }
        }

        int level = state.get(SoulRoseBlock.LEVEL, 0);
        SoulRoseType type = state.get(SoulRoseBlock.TYPE, SoulRoseType.NONE);
        if (level == 0 || type == SoulRoseType.NONE) return;

        // Copper undead attack
        if (world.getTime() % 30 == 0L) {
            if (type == SoulRoseType.GOLD) {
                tryAttack((ServerWorld) world, pos, state, blockEntity);
            }
        }
    }

    private static void tryAttack(ServerWorld world, BlockPos pos, BlockState state, SoulRoseBlockEntity blockEntity) {
        int level = state.get(SoulRoseBlock.LEVEL, 0);

        List<LivingEntity> list = world.getEntitiesByClass(LivingEntity.class, getAttackZone(pos, level), (entity) -> entity.getType().isIn(EntityTypeTags.UNDEAD) && !entity.hasCustomName());
        for (LivingEntity livingEntity : list) {
            if (livingEntity == null) continue;
            if (livingEntity.hasStatusEffect(ModEffects.SOUL_SIPHON)) continue;
            if (livingEntity.isAlive() && pos.isWithinDistance(livingEntity.getBlockPos(), levelToRadius(level))) {
                livingEntity.addStatusEffect(new StatusEffectInstance(ModEffects.SOUL_SIPHON, 320, level, false, true, false));
            }
        }

    }

    private static Box getAttackZone(BlockPos pos, int level) {
        double xz = levelToRadius(level);
        return (new Box(pos)).expand(xz, 3, xz);
    }

    private static double levelToRadius(int level) {
        return (level == 3) ? AffectorType.SOUL_ROSE_COPPER_3.width : (level == 2) ? AffectorType.SOUL_ROSE_COPPER_2.width : AffectorType.SOUL_ROSE_COPPER_1.width;
    }

    private static int updateLevel(World world, BlockPos pos) {
        if (!world.getBlockState(pos.up()).isOf(Blocks.AIR) && !world.getBlockState(pos.up()).isOf(ModBlocks.SOUL_ROSE)) return 0;

        BlockState core = world.getBlockState(pos.add(0, -2, 0));
        Block coreBlock = core.getBlock();
        if (!CORE_MATERIALS.test(core)) return 0;
        int level = 0;

        // Stage 1
        for (Vec3i offset : STAGE_1A) {
            if (!world.getBlockState(pos.add(offset)).isOf(coreBlock)) return level;
        }
        for (int i = 0; i < 4; i++) {
            for (Vec3i offset : STAGE_1B) {
                if (!world.getBlockState(pos.add(rotate(offset, i))).isOf(coreBlock)) return level;
            }
        }
        level += 1;

        // Stage 2
        for (int i = 0; i < 4; i++) {
            for (Vec3i offset : STAGE_2) {
                if (!world.getBlockState(pos.add(rotate(offset, i))).isOf(coreBlock)) return level;
            }
        }
        level += 1;

        // Stage 3
        for (int i = 0; i < 4; i++) {
            for (Vec3i offset : STAGE_3) {
                if (!world.getBlockState(pos.add(rotate(offset, i))).isOf(coreBlock)) return level;
            }
        }

        return 3;
    }

    private static Vec3i rotate(Vec3i v, int dir) {
        int x = v.getX();
        int y = v.getY();
        int z = v.getZ();

        return switch (dir) {
            case 0  -> new Vec3i( x, y,  z);
            case 1 -> new Vec3i(-z, y,  x);
            case 2  -> new Vec3i(-x, y, -z);
            case 3 -> new Vec3i( z, y, -x);
            default -> v;
        };
    }
}
