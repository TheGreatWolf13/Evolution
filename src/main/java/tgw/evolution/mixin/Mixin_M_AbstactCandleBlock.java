package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCandleBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.hooks.asm.DeleteMethod;
import tgw.evolution.util.math.MathHelper;

import java.util.Random;
import java.util.random.RandomGenerator;

@Mixin(AbstractCandleBlock.class)
public abstract class Mixin_M_AbstactCandleBlock extends Block {

    @Shadow @Final public static BooleanProperty LIT;

    public Mixin_M_AbstactCandleBlock(Properties properties) {
        super(properties);
    }

    @Shadow
    private static void addParticlesAndSound(Level level, Vec3 vec3, Random random) {
        throw new AbstractMethodError();
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public void animateTick(BlockState state, Level level, BlockPos pos, Random random) {
        throw new AbstractMethodError();
    }

    @Override
    public void animateTick_(BlockState state, Level level, int x, int y, int z, RandomGenerator random) {
        if (state.getValue(LIT)) {
            this.getParticleOffsets(state).forEach(vec3 -> addParticlesAndSound(level, vec3.add(x, y, z), MathHelper.RANDOM));
        }
    }

    @Shadow
    protected abstract Iterable<Vec3> getParticleOffsets(BlockState blockState);
}
