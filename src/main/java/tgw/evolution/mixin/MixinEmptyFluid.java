package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.material.EmptyFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tgw.evolution.Evolution;

@Mixin(EmptyFluid.class)
public abstract class MixinEmptyFluid extends Fluid {

    /**
     * @reason Deprecated
     */
    @Override
    @Overwrite
    public float getHeight(FluidState fluidState, BlockGetter level, BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.getHeight_(fluidState, level, pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public float getHeight_(FluidState fluidState, BlockGetter level, int x, int y, int z) {
        return 0;
    }

    /**
     * @reason Deprecated
     */
    @Override
    @Overwrite
    public VoxelShape getShape(FluidState fluidState, BlockGetter level, BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.getShape_(fluidState, level, pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public VoxelShape getShape_(FluidState fluidState, BlockGetter level, int x, int y, int z) {
        return Shapes.empty();
    }
}
