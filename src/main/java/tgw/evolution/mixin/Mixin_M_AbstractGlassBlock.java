package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.AbstractGlassBlock;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tgw.evolution.hooks.asm.DeleteMethod;

@Mixin(AbstractGlassBlock.class)
public abstract class Mixin_M_AbstractGlassBlock extends HalfTransparentBlock {

    public Mixin_M_AbstractGlassBlock(Properties properties) {
        super(properties);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @DeleteMethod
    @Overwrite
    public float getShadeBrightness(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        throw new AbstractMethodError();
    }

    @Override
    public float getShadeBrightness_(BlockState state, BlockGetter level, int x, int y, int z) {
        return 1.0f;
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public VoxelShape getVisualShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        throw new AbstractMethodError();
    }

    @Override
    public VoxelShape getVisualShape_(BlockState state, BlockGetter level, int x, int y, int z, @Nullable Entity entity) {
        return Shapes.empty();
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @DeleteMethod
    @Overwrite
    public boolean propagatesSkylightDown(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        throw new AbstractMethodError();
    }

    @Override
    public boolean propagatesSkylightDown_(BlockState state, BlockGetter level, int x, int y, int z) {
        return true;
    }
}
