package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.DetectorRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.hooks.asm.DeleteMethod;

import java.util.Random;

@Mixin(DetectorRailBlock.class)
public abstract class Mixin_M_DetectorRailBlock extends BaseRailBlock {

    @Shadow @Final public static BooleanProperty POWERED;

    public Mixin_M_DetectorRailBlock(boolean bl, Properties properties) {
        super(bl, properties);
    }

    @Shadow
    protected abstract void checkPressed(Level level, BlockPos blockPos, BlockState blockState);

    @Override
    @Overwrite
    @DeleteMethod
    public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        throw new AbstractMethodError();
    }

    @Override
    public void onPlace_(BlockState state, Level level, int x, int y, int z, BlockState oldState, boolean isMoving) {
        if (!oldState.is(state.getBlock())) {
            BlockPos pos = new BlockPos(x, y, z);
            BlockState updatedState = this.updateState(state, level, pos, isMoving);
            this.checkPressed(level, pos, updatedState);
        }
    }

    @Override
    @Overwrite
    @DeleteMethod
    public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        throw new AbstractMethodError();
    }

    @Override
    public void tick_(BlockState state, ServerLevel level, int x, int y, int z, Random random) {
        if (state.getValue(POWERED)) {
            this.checkPressed(level, new BlockPos(x, y, z), state);
        }
    }
}
