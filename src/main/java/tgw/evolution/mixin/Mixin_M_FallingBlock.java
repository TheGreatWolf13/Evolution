package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Fallable;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.hooks.asm.DeleteMethod;

import java.util.Random;
import java.util.random.RandomGenerator;

@Mixin(FallingBlock.class)
public abstract class Mixin_M_FallingBlock extends Block implements Fallable {

    public Mixin_M_FallingBlock(Properties properties) {
        super(properties);
    }

    @Shadow
    public static boolean isFree(BlockState blockState) {
        throw new AbstractMethodError();
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
        if (random.nextInt(16) == 0) {
            if (isFree(level.getBlockState_(x, y - 1, z))) {
                level.addParticle(new BlockParticleOption(ParticleTypes.FALLING_DUST, state), x + random.nextDouble(), y - 0.05, z + random.nextDouble(), 0, 0, 0);
            }
        }
    }

    @Shadow
    protected abstract void falling(FallingBlockEntity fallingBlockEntity);

    @Shadow
    protected abstract int getDelayAfterPlace();

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        throw new AbstractMethodError();
    }

    @Override
    public void onPlace_(BlockState state, Level level, int x, int y, int z, BlockState oldState, boolean isMoving) {
        level.scheduleTick(new BlockPos(x, y, z), this, this.getDelayAfterPlace());
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        throw new AbstractMethodError();
    }

    @Override
    public void tick_(BlockState state, ServerLevel level, int x, int y, int z, Random random) {
        if (isFree(level.getBlockState_(x, y - 1, z)) && y >= level.getMinBuildHeight()) {
            this.falling(FallingBlockEntity.fall(level, new BlockPos(x, y, z), state));
        }
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public BlockState updateShape(BlockState blockState,
                                  Direction direction,
                                  BlockState blockState2,
                                  LevelAccessor levelAccessor,
                                  BlockPos blockPos,
                                  BlockPos blockPos2) {
        throw new AbstractMethodError();
    }

    @Override
    public BlockState updateShape_(BlockState state,
                                   Direction from,
                                   BlockState fromState,
                                   LevelAccessor level,
                                   int x,
                                   int y,
                                   int z,
                                   int fromX,
                                   int fromY,
                                   int fromZ) {
        level.scheduleTick(new BlockPos(x, y, z), this, this.getDelayAfterPlace());
        return super.updateShape_(state, from, fromState, level, x, y, z, fromX, fromY, fromZ);
    }
}
