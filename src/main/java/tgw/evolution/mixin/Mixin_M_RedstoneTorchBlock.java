package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.RedstoneTorchBlock;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.Contract;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.hooks.asm.DeleteMethod;
import tgw.evolution.util.math.DirectionUtil;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.random.RandomGenerator;

@Mixin(RedstoneTorchBlock.class)
public abstract class Mixin_M_RedstoneTorchBlock extends TorchBlock {

    @Shadow @Final public static BooleanProperty LIT;
    @Shadow @Final private static Map<BlockGetter, List<RedstoneTorchBlock.Toggle>> RECENT_TOGGLES;

    public Mixin_M_RedstoneTorchBlock(Properties properties, ParticleOptions particleOptions) {
        super(properties, particleOptions);
    }

    @Contract(value = "_, _, _ -> _")
    @Shadow
    private static boolean isToggledTooFrequently(Level level, BlockPos blockPos, boolean bl) {
        //noinspection Contract
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
        if (state.getValue(LIT)) {
            level.addParticle(this.flameParticle,
                              x + 0.5 + (random.nextDouble() - 0.5) * 0.2, y + 0.7 + (random.nextDouble() - 0.5) * 0.2, z + 0.5 + (random.nextDouble() - 0.5) * 0.2,
                              0, 0, 0
            );
        }
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public int getSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        throw new AbstractMethodError();
    }

    @Override
    public int getSignal_(BlockState state, BlockGetter level, int x, int y, int z, Direction dir) {
        return state.getValue(LIT) && Direction.UP != dir ? 15 : 0;
    }

    @Shadow
    protected abstract boolean hasNeighborSignal(Level level, BlockPos blockPos, BlockState blockState);

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
        throw new AbstractMethodError();
    }

    @Override
    public void neighborChanged_(BlockState state,
                                 Level level,
                                 int x,
                                 int y,
                                 int z,
                                 Block oldBlock,
                                 int fromX,
                                 int fromY,
                                 int fromZ,
                                 boolean isMoving) {
        BlockPos pos = new BlockPos(x, y, z);
        if (state.getValue(LIT) == this.hasNeighborSignal(level, pos, state) && !level.getBlockTicks().willTickThisTick(pos, this)) {
            level.scheduleTick(pos, this, 2);
        }
    }

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
        BlockPos pos = new BlockPos(x, y, z);
        for (Direction direction : DirectionUtil.ALL) {
            level.updateNeighborsAt(pos.relative(direction), this);
        }
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        throw new AbstractMethodError();
    }

    @Override
    public void onRemove_(BlockState state, Level level, int x, int y, int z, BlockState newState, boolean isMoving) {
        if (!isMoving) {
            for (Direction dir : DirectionUtil.ALL) {
                int offX = x + dir.getStepX();
                int offY = y + dir.getStepY();
                int offZ = z + dir.getStepZ();
                level.updateNeighborsAt_(offX, offY, offZ, this);
            }
        }
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
        BlockPos pos = new BlockPos(x, y, z);
        boolean bl = this.hasNeighborSignal(level, pos, state);
        List<RedstoneTorchBlock.Toggle> list = RECENT_TOGGLES.get(level);
        while (list != null && !list.isEmpty() && level.getGameTime() - list.get(0).when > 60L) {
            list.remove(0);
        }
        if (state.getValue(LIT)) {
            if (bl) {
                level.setBlockAndUpdate_(x, y, z, state.setValue(LIT, false));
                if (isToggledTooFrequently(level, pos, true)) {
                    level.levelEvent_(LevelEvent.REDSTONE_TORCH_BURNOUT, x, y, z, 0);
                    level.scheduleTick(pos, level.getBlockState_(x, y, z).getBlock(), 160);
                }
            }
        }
        else if (!bl && !isToggledTooFrequently(level, pos, false)) {
            level.setBlockAndUpdate_(x, y, z, state.setValue(LIT, true));
        }
    }
}
