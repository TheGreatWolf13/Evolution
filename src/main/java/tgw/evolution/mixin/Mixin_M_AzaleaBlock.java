package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.AzaleaBlock;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.hooks.asm.DeleteMethod;

@Mixin(AzaleaBlock.class)
public abstract class Mixin_M_AzaleaBlock extends BushBlock implements BonemealableBlock {

    @Shadow @Final private static VoxelShape SHAPE;

    public Mixin_M_AzaleaBlock(Properties properties) {
        super(properties);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        throw new AbstractMethodError();
    }

    @Override
    public VoxelShape getShape_(BlockState state, BlockGetter level, int x, int y, int z, @Nullable Entity entity) {
        return SHAPE;
    }
}
