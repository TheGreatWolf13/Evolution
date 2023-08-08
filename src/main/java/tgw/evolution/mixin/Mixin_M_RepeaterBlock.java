package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.RepeaterBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.hooks.asm.DeleteMethod;

import java.util.Random;
import java.util.random.RandomGenerator;

@Mixin(RepeaterBlock.class)
public abstract class Mixin_M_RepeaterBlock extends DiodeBlock {

    @Shadow @Final public static IntegerProperty DELAY;
    @Shadow @Final public static BooleanProperty LOCKED;

    public Mixin_M_RepeaterBlock(Properties properties) {
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
        if (state.getValue(POWERED)) {
            Direction direction = state.getValue(FACING);
            double px = x + 0.5 + (random.nextDouble() - 0.5) * 0.2;
            double py = y + 0.4 + (random.nextDouble() - 0.5) * 0.2;
            double pz = z + 0.5 + (random.nextDouble() - 0.5) * 0.2;
            float g = -5.0F;
            if (random.nextBoolean()) {
                g = state.getValue(DELAY) * 2 - 1;
            }
            g /= 16.0F;
            double h = g * direction.getStepX();
            double i = g * direction.getStepZ();
            level.addParticle(DustParticleOptions.REDSTONE, px + h, py, pz + i, 0, 0, 0);
        }
    }

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
        return !level.isClientSide() && from.getAxis() != state.getValue(FACING).getAxis() ?
               state.setValue(LOCKED, this.isLocked(level, new BlockPos(x, y, z), state)) :
               super.updateShape_(state, from, fromState, level, x, y, z, fromX, fromY, fromZ);
    }

    @Override
    @Overwrite
    @DeleteMethod
    public InteractionResult use(BlockState blockState,
                                 Level level,
                                 BlockPos blockPos,
                                 Player player,
                                 InteractionHand interactionHand,
                                 BlockHitResult blockHitResult) {
        throw new AbstractMethodError();
    }

    @Override
    public InteractionResult use_(BlockState state, Level level, int x, int y, int z, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!player.getAbilities().mayBuild) {
            return InteractionResult.PASS;
        }
        level.setBlock(new BlockPos(x, y, z), state.cycle(DELAY), 3);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
