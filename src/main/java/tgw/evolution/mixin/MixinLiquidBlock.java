package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(LiquidBlock.class)
public abstract class MixinLiquidBlock extends Block {

    @Shadow @Final public static VoxelShape STABLE_SHAPE;

    @Shadow @Final public static IntegerProperty LEVEL;

    public MixinLiquidBlock(Properties properties) {
        super(properties);
    }

    /**
     * @author TheGreatWolf
     * @reason Call non-BlockPos version
     */
    @Override
    @Overwrite
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return context.isAbove(STABLE_SHAPE, pos, true) &&
               state.getValue(LEVEL) == 0 &&
               context.canStandOnFluid(level.getFluidState_(pos.getX(), pos.getY() + 1, pos.getZ()), state.getFluidState()) ?
               STABLE_SHAPE :
               Shapes.empty();
    }
}
