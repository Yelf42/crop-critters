package yelf42.cropcritters.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.block.LanternBlock;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;

public class LostSoulInAJarBlock extends LanternBlock {

    public LostSoulInAJarBlock(Settings settings) {
        super(settings);
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (random.nextDouble() <= 0.7) {
            double d = (double)pos.getX() + random.nextDouble() * (double)10.0F - (double)5.0F;
            double e = (double)pos.getY() + random.nextDouble() * (double)5.0F;
            double f = (double)pos.getZ() + random.nextDouble() * (double)10.0F - (double)5.0F;
            world.addParticleClient(ParticleTypes.SOUL, d, e, f, (double)0.0F, (double)0.0F, (double)0.0F);
        }

    }

}
