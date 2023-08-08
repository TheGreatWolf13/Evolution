package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.material.EmptyFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tgw.evolution.Evolution;
import tgw.evolution.hooks.asm.DeleteMethod;
import tgw.evolution.util.math.Vec3d;

@Mixin(EmptyFluid.class)
public abstract class Mixin_M_EmptyFluid extends Fluid {

    @Override
    public tgw.evolution.util.physics.Fluid fluid() {
        return tgw.evolution.util.physics.Fluid.AIR;
    }

    @Override
    public Vec3d getFlow(BlockGetter level, int x, int y, int z, FluidState fluidState, Vec3d flow) {
        return flow;
    }

    @Override
    @Overwrite
    @DeleteMethod
    public Vec3 getFlow(BlockGetter blockGetter, BlockPos blockPos, FluidState fluidState) {
        return Vec3.ZERO;
    }

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
