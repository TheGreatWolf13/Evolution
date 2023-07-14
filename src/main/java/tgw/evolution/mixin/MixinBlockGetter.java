package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipBlockStateContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.Evolution;
import tgw.evolution.patches.PatchBlockGetter;

import java.util.Optional;

@Mixin(BlockGetter.class)
public interface MixinBlockGetter extends PatchBlockGetter {

    /**
     * @reason Deprecated
     */
    @Overwrite
    private static BlockHitResult method_32882(ClipBlockStateContext c) {
        Vec3 from = c.getFrom();
        Vec3 to = c.getTo();
        double x = from.x - to.x;
        double y = from.y - to.y;
        double z = from.z - to.z;
        return BlockHitResult.miss(to, Direction.getNearest(x, y, z), new BlockPos(to));
    }

    @Shadow
    @Nullable BlockHitResult clipWithInteractionOverride(Vec3 vec3,
                                                         Vec3 vec32,
                                                         BlockPos blockPos,
                                                         VoxelShape voxelShape,
                                                         BlockState blockState);

    /**
     * @author TheGreatWolf
     * @reason Use non-BlockPos implementation
     */
    @Overwrite
    default <T extends BlockEntity> Optional<T> getBlockEntity(BlockPos pos, BlockEntityType<T> type) {
        Evolution.deprecatedMethod();
        return Optional.ofNullable(this.getBlockEntity_(pos.getX(), pos.getY(), pos.getZ(), type));
    }

    /**
     * @author TheGreatWolf
     * @reason Use non-BlockPos implementation
     */
    @Overwrite
    default int getLightEmission(BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.getLightEmission_(pos.getX(), pos.getY(), pos.getZ());
    }

    /**
     * @reason Deprecated
     */
    @Overwrite
    default BlockHitResult isBlockInLine(ClipBlockStateContext context) {
        Vec3 from = context.getFrom();
        Vec3 to = context.getTo();
        if (from.equals(to)) {
            return method_32882(context);
        }
        double x0 = Mth.lerp(-1.0E-7, from.x, to.x);
        double y0 = Mth.lerp(-1.0E-7, from.y, to.y);
        double z0 = Mth.lerp(-1.0E-7, from.z, to.z);
        int x = Mth.floor(x0);
        int y = Mth.floor(y0);
        int z = Mth.floor(z0);
        BlockState state = this.getBlockState_(x, y, z);
        double vx = from.x - to.x;
        double vy = from.y - to.y;
        double vz = from.z - to.z;
        BlockHitResult hitResult = context.isTargetBlock().test(state) ?
                                   new BlockHitResult(to, Direction.getNearest(vx, vy, vz), new BlockPos(to), false) : null;
        if (hitResult != null) {
            return hitResult;
        }
        double x1 = Mth.lerp(-1.0E-7, to.x, from.x);
        double y1 = Mth.lerp(-1.0E-7, to.y, from.y);
        double z1 = Mth.lerp(-1.0E-7, to.z, from.z);
        double dx = x1 - x0;
        double dy = y1 - y0;
        double dz = z1 - z0;
        int sx = Mth.sign(dx);
        int sy = Mth.sign(dy);
        int sz = Mth.sign(dz);
        double ddx = sx == 0 ? Double.MAX_VALUE : sx / dx;
        double ddy = sy == 0 ? Double.MAX_VALUE : sy / dy;
        double ddz = sz == 0 ? Double.MAX_VALUE : sz / dz;
        double dddx = ddx * (sx > 0 ? 1.0 - Mth.frac(x0) : Mth.frac(x0));
        double dddy = ddy * (sy > 0 ? 1.0 - Mth.frac(y0) : Mth.frac(y0));
        double dddz = ddz * (sz > 0 ? 1.0 - Mth.frac(z0) : Mth.frac(z0));
        while (true) {
            if (!(dddx <= 1.0) && !(dddy <= 1.0) && !(dddz <= 1.0)) {
                return method_32882(context);
            }
            if (dddx < dddy) {
                if (dddx < dddz) {
                    x += sx;
                    dddx += ddx;
                }
                else {
                    z += sz;
                    dddz += ddz;
                }
            }
            else if (dddy < dddz) {
                y += sy;
                dddy += ddy;
            }
            else {
                z += sz;
                dddz += ddz;
            }
            state = this.getBlockState_(x, y, z);
            vx = from.x - to.x;
            vy = from.y - to.y;
            vz = from.z - to.z;
            if (context.isTargetBlock().test(state)) {
                return new BlockHitResult(to, Direction.getNearest(vx, vy, vz), new BlockPos(to), false);
            }
        }
    }

    /**
     * @reason Deprecated
     */
    @Overwrite
    private BlockHitResult method_17743(ClipContext context, BlockPos pos) {
        BlockState blockState = this.getBlockState_(pos);
        FluidState fluidState = this.getFluidState_(pos);
        VoxelShape shape = context.getBlockShape(blockState, (BlockGetter) this, pos);
        Vec3 from = context.getFrom();
        Vec3 to = context.getTo();
        BlockHitResult blockHitResult = this.clipWithInteractionOverride(from, to, pos, shape, blockState);
        VoxelShape fluidShape = context.getFluidShape(fluidState, (BlockGetter) this, pos);
        BlockHitResult fluidHitResult = fluidShape.clip(from, to, pos);
        double blockDist = blockHitResult == null ? Double.MAX_VALUE : from.distanceToSqr(blockHitResult.getLocation());
        double fluidDist = fluidHitResult == null ? Double.MAX_VALUE : from.distanceToSqr(fluidHitResult.getLocation());
        //noinspection ConstantConditions
        return blockDist <= fluidDist ? blockHitResult : fluidHitResult;
    }
}
