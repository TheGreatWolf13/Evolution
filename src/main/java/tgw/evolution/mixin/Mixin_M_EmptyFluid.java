package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    public boolean canBeReplacedWith(FluidState state, BlockGetter level, BlockPos pos, Fluid fluid, Direction direction) {
        Evolution.deprecatedMethod();
        return this.canBeReplacedWith_(state, level, pos.getX(), pos.getY(), pos.getZ(), fluid, direction);
    }

    @Override
    public boolean canBeReplacedWith_(FluidState state, BlockGetter level, int x, int y, int z, Fluid fluid, Direction direction) {
        return true;
    }

    @Override
    public tgw.evolution.util.physics.Fluid fluid() {
        return tgw.evolution.util.physics.Fluid.AIR;
    }

    @Override
    public Vec3d getFlow(BlockGetter level, int x, int y, int z, FluidState fluidState, Vec3d flow) {
        return flow;
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public Vec3 getFlow(BlockGetter blockGetter, BlockPos blockPos, FluidState fluidState) {
        return Vec3.ZERO;
    }

    /**
     * @reason _
     * @author TheGreatWolf
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
     * @reason _
     * @author TheGreatWolf
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
