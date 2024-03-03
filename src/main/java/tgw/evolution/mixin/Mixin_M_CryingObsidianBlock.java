package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CryingObsidianBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tgw.evolution.hooks.asm.DeleteMethod;
import tgw.evolution.util.math.DirectionUtil;

import java.util.Random;
import java.util.random.RandomGenerator;

@Mixin(CryingObsidianBlock.class)
public abstract class Mixin_M_CryingObsidianBlock extends Block {

    public Mixin_M_CryingObsidianBlock(Properties properties) {
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
            Direction dir = DirectionUtil.getRandom(random);
            if (dir != Direction.UP) {
                int stepX = dir.getStepX();
                int stepY = dir.getStepY();
                int stepZ = dir.getStepZ();
                int offX = x + stepX;
                int offY = y + stepY;
                int offZ = z + stepZ;
                BlockState stateAtDir = level.getBlockState_(offX, offY, offZ);
                if (!state.canOcclude() || !stateAtDir.isFaceSturdy_(level, offX, offY, offZ, dir.getOpposite())) {
                    double d = stepX == 0 ? random.nextDouble() : 0.5 + stepX * 0.6;
                    double e = stepY == 0 ? random.nextDouble() : 0.5 + stepY * 0.6;
                    double f = stepZ == 0 ? random.nextDouble() : 0.5 + stepZ * 0.6;
                    level.addParticle(ParticleTypes.DRIPPING_OBSIDIAN_TEAR, x + d, y + e, z + f, 0, 0, 0);
                }
            }
        }
    }
}
