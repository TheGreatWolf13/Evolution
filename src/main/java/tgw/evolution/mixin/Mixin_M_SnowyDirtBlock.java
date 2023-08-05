package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SnowyDirtBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.hooks.asm.DeleteMethod;

@Mixin(SnowyDirtBlock.class)
public abstract class Mixin_M_SnowyDirtBlock extends Block {

    @Shadow @Final public static BooleanProperty SNOWY;

    public Mixin_M_SnowyDirtBlock(Properties properties) {
        super(properties);
    }

    @Shadow
    private static boolean isSnowySetting(BlockState blockState) {
        throw new AbstractMethodError();
    }

    @Override
    @Overwrite
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        BlockState stateAbove = context.getLevel().getBlockState_(pos.getX(), pos.getY() + 1, pos.getZ());
        return this.defaultBlockState().setValue(SNOWY, isSnowySetting(stateAbove));
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
        return from == Direction.UP ?
               state.setValue(SNOWY, isSnowySetting(fromState)) :
               super.updateShape_(state, from, fromState, level, x, y, z, fromX, fromY, fromZ);
    }
}
