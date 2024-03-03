package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.AbstractBannerBlock;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.hooks.asm.DeleteMethod;

@Mixin(BannerBlock.class)
public abstract class Mixin_M_BannerBlock extends AbstractBannerBlock {

    @Shadow @Final private static VoxelShape SHAPE;

    public Mixin_M_BannerBlock(DyeColor dyeColor, Properties properties) {
        super(dyeColor, properties);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        throw new AbstractMethodError();
    }

    @Override
    public boolean canSurvive_(BlockState state, LevelReader level, int x, int y, int z) {
        return level.getBlockState_(x, y - 1, z).getMaterial().isSolid();
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
        return from == Direction.DOWN && !state.canSurvive_(level, x, y, z) ?
               Blocks.AIR.defaultBlockState() :
               super.updateShape_(state, from, fromState, level, x, y, z, fromX, fromY, fromZ);
    }
}
