package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.WitherRoseBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tgw.evolution.hooks.asm.DeleteMethod;

import java.util.Random;
import java.util.random.RandomGenerator;

@Mixin(WitherRoseBlock.class)
public abstract class Mixin_M_WitherRoseBlock extends FlowerBlock {

    public Mixin_M_WitherRoseBlock(MobEffect mobEffect, int i, Properties properties) {
        super(mobEffect, i, properties);
    }

    @Override
    @Overwrite
    @DeleteMethod
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, Random random) {
        throw new AbstractMethodError();
    }

    @Override
    public void animateTick_(BlockState state, Level level, int x, int y, int z, RandomGenerator random) {
        VoxelShape shape = this.getShape_(state, level, x, y, z, null);
        double px = x + (shape.min(Direction.Axis.X) + shape.max(Direction.Axis.X)) * 0.5;
        double pz = z + (shape.min(Direction.Axis.Z) + shape.max(Direction.Axis.Z)) * 0.5;
        for (int i = 0; i < 3; ++i) {
            if (random.nextBoolean()) {
                level.addParticle(ParticleTypes.SMOKE, px + random.nextDouble() / 5, y + (0.5 - random.nextDouble()), pz + random.nextDouble() / 5, 0, 0, 0);
            }
        }
    }
}
