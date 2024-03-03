package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.Evolution;
import tgw.evolution.hooks.asm.DeleteField;
import tgw.evolution.hooks.asm.ModifyConstructor;
import tgw.evolution.hooks.asm.RestoreFinal;
import tgw.evolution.patches.PatchClipContext;

@Mixin(ClipContext.class)
public abstract class Mixin_CF_ClipContext implements PatchClipContext {

    @Mutable @Shadow @Final private ClipContext.Block block;
    @Shadow @Final @DeleteField private CollisionContext collisionContext;
    @Unique private @Nullable Entity entity;
    @Mutable @Shadow @Final private ClipContext.Fluid fluid;
    @Mutable @Shadow @Final @RestoreFinal private Vec3 from;
    @Mutable @Shadow @Final @RestoreFinal private Vec3 to;

    @ModifyConstructor
    public Mixin_CF_ClipContext(Vec3 from, Vec3 to, ClipContext.Block block, ClipContext.Fluid fluid, Entity entity) {
        this.from = from;
        this.to = to;
        this.block = block;
        this.fluid = fluid;
        this.entity = entity;
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public VoxelShape getBlockShape(BlockState state, BlockGetter level, BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.getBlockShape_(state, level, pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public VoxelShape getBlockShape_(BlockState state, BlockGetter level, int x, int y, int z) {
        return this.block.get_(state, level, x, y, z, this.entity);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public VoxelShape getFluidShape(FluidState state, BlockGetter level, BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.getFluidShape_(state, level, pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public VoxelShape getFluidShape_(FluidState state, BlockGetter level, int x, int y, int z) {
        return this.fluid.canPick(state) ? state.getShape_(level, x, y, z) : Shapes.empty();
    }

    @Override
    public void setBlock(ClipContext.Block block) {
        this.block = block;
    }

    @Override
    public void setEntity(@Nullable Entity entity) {
        this.entity = entity;
    }

    @Override
    public void setFluid(ClipContext.Fluid fluid) {
        this.fluid = fluid;
    }
}
