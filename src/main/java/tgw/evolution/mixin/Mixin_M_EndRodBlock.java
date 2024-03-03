package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EndRodBlock;
import net.minecraft.world.level.block.RodBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tgw.evolution.hooks.asm.DeleteMethod;

import java.util.Random;
import java.util.random.RandomGenerator;

@Mixin(EndRodBlock.class)
public abstract class Mixin_M_EndRodBlock extends RodBlock {

    public Mixin_M_EndRodBlock(Properties properties) {
        super(properties);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, Random random) {
        throw new AbstractMethodError();
    }

    @Override
    public void animateTick_(BlockState state, Level level, int x, int y, int z, RandomGenerator random) {
        if (random.nextInt(5) == 0) {
            Direction dir = state.getValue(FACING);
            double mult = 0.4F - (random.nextFloat() + random.nextFloat()) * 0.4F;
            level.addParticle(ParticleTypes.END_ROD,
                              x + 0.55 - random.nextFloat() * 0.1F + dir.getStepX() * mult,
                              y + 0.55 - random.nextFloat() * 0.1F + dir.getStepY() * mult,
                              z + 0.55 - random.nextFloat() * 0.1F + dir.getStepZ() * mult,
                              random.nextGaussian() * 0.005,
                              random.nextGaussian() * 0.005,
                              random.nextGaussian() * 0.005
            );
        }
    }
}
