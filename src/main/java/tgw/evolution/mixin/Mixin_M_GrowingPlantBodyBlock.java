package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.GrowingPlantBlock;
import net.minecraft.world.level.block.GrowingPlantBodyBlock;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.hooks.asm.DeleteMethod;

@Mixin(GrowingPlantBodyBlock.class)
public abstract class Mixin_M_GrowingPlantBodyBlock extends GrowingPlantBlock implements BonemealableBlock {

    public Mixin_M_GrowingPlantBodyBlock(Properties properties,
                                         Direction direction,
                                         VoxelShape voxelShape, boolean bl) {
        super(properties, direction, voxelShape, bl);
    }

    @Shadow
    protected abstract BlockState updateHeadAfterConvertedFromBody(BlockState blockState, BlockState blockState2);

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
        if (from == this.growthDirection.getOpposite() && !state.canSurvive_(level, x, y, z)) {
            level.scheduleTick(new BlockPos(x, y, z), this, 1);
        }
        GrowingPlantHeadBlock growingPlantHeadBlock = this.getHeadBlock();
        if (from == this.growthDirection && !fromState.is(this) && !fromState.is(growingPlantHeadBlock)) {
            return this.updateHeadAfterConvertedFromBody(state, growingPlantHeadBlock.getStateForPlacement(level));
        }
        if (this.scheduleFluidTicks) {
            level.scheduleTick(new BlockPos(x, y, z), Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape_(state, from, fromState, level, x, y, z, fromX, fromY, fromZ);
    }
}
