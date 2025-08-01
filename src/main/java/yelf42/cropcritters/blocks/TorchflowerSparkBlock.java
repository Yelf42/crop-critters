package yelf42.cropcritters.blocks;

import net.minecraft.block.AirBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.tick.TickPriority;
import org.jetbrains.annotations.Nullable;
import yelf42.cropcritters.CropCritters;
import yelf42.cropcritters.entity.TorchflowerCritterEntity;

import java.util.List;

public class TorchflowerSparkBlock extends AirBlock {

    public TorchflowerSparkBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        List<TorchflowerCritterEntity> list = world.getEntitiesByClass(TorchflowerCritterEntity.class, new Box(pos).expand(2F), (torchflowerCritterEntity -> true));
        if (list.isEmpty()) {
            world.setBlockState(pos, Blocks.AIR.getDefaultState());
        } else {
            world.scheduleBlockTick(pos, ModBlocks.TORCHFLOWER_SPARK, 200, TickPriority.EXTREMELY_LOW);
        }
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        super.randomDisplayTick(state, world, pos, random);
        if (world.isClient) {
            Vec3d p = pos.toCenterPos();
            world.addParticleClient(ParticleTypes.SOUL_FIRE_FLAME, p.x, p.y, p.z, 0F, 0F, 0F);
        }
    }
}
