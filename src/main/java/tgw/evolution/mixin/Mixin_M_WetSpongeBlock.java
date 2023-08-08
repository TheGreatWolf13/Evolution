package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.WetSpongeBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tgw.evolution.blocks.util.BlockUtils;
import tgw.evolution.hooks.asm.DeleteMethod;
import tgw.evolution.util.math.DirectionUtil;

import java.util.Random;
import java.util.random.RandomGenerator;

@Mixin(WetSpongeBlock.class)
public abstract class Mixin_M_WetSpongeBlock extends Block {

    public Mixin_M_WetSpongeBlock(Properties properties) {
        super(properties);
    }

    @Override
    @Overwrite
    @DeleteMethod
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, Random random) {
        throw new AbstractMethodError();
    }

    @Override
    public void animateTick_(BlockState state, Level level, int x, int y, int z, RandomGenerator random) {
        Direction dir = DirectionUtil.getRandom(random);
        if (dir != Direction.UP) {
            if (!state.canOcclude() || !BlockUtils.hasSolidFaceAtSide(level, x, y, z, dir)) {
                double px = x;
                double py = y;
                double pz = z;
                if (dir == Direction.DOWN) {
                    py -= 0.05;
                    px += random.nextDouble();
                    pz += random.nextDouble();
                }
                else {
                    py += random.nextDouble() * 0.8;
                    if (dir.getAxis() == Direction.Axis.X) {
                        pz += random.nextDouble();
                        if (dir == Direction.EAST) {
                            ++px;
                        }
                        else {
                            px += 0.05;
                        }
                    }
                    else {
                        px += random.nextDouble();
                        if (dir == Direction.SOUTH) {
                            ++pz;
                        }
                        else {
                            pz += 0.05;
                        }
                    }
                }
                level.addParticle(ParticleTypes.DRIPPING_WATER, px, py, pz, 0, 0, 0);
            }
        }
    }

    @Override
    @Overwrite
    @DeleteMethod
    public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        throw new AbstractMethodError();
    }

    @Override
    public void onPlace_(BlockState state, Level level, int x, int y, int z, BlockState oldState, boolean isMoving) {
        if (level.dimensionType().ultraWarm()) {
            BlockPos pos = new BlockPos(x, y, z);
            level.setBlock(pos, Blocks.SPONGE.defaultBlockState(), 3);
            level.levelEvent(LevelEvent.PARTICLES_WATER_EVAPORATING, pos, 0);
            level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, (1.0F + level.getRandom().nextFloat() * 0.2F) * 0.7F);
        }
    }
}
