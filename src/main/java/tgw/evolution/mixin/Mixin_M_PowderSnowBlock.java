package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.PowderSnowBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.entities.EntityUtils;
import tgw.evolution.hooks.asm.DeleteMethod;

@Mixin(PowderSnowBlock.class)
public abstract class Mixin_M_PowderSnowBlock extends Block implements BucketPickup {

    @Shadow @Final private static VoxelShape FALLING_COLLISION_SHAPE;

    public Mixin_M_PowderSnowBlock(Properties properties) {
        super(properties);
    }

    @Shadow
    public static boolean canEntityWalkOnPowderSnow(Entity entity) {
        throw new AbstractMethodError();
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        throw new AbstractMethodError();
    }

    @Override
    public VoxelShape getCollisionShape_(BlockState state, BlockGetter level, int x, int y, int z, @Nullable Entity entity) {
        if (entity != null) {
            if (entity.fallDistance > 2.5F) {
                return FALLING_COLLISION_SHAPE;
            }
            //noinspection ConstantConditions
            if (entity instanceof FallingBlockEntity ||
                canEntityWalkOnPowderSnow(entity) &&
                EntityUtils.isAbove(entity, Shapes.block(), y) &&
                !entity.isDescending()) {
                return super.getCollisionShape_(state, level, x, y, z, entity);
            }
        }
        return Shapes.empty();
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public VoxelShape getOcclusionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        throw new AbstractMethodError();
    }

    @Override
    public VoxelShape getOcclusionShape_(BlockState state, BlockGetter level, int x, int y, int z) {
        return Shapes.empty();
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
}
