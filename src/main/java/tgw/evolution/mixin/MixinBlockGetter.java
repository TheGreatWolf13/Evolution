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
import org.spongepowered.asm.mixin.Unique;
import tgw.evolution.Evolution;
import tgw.evolution.patches.PatchBlockGetter;
import tgw.evolution.patches.PatchBlockHitResult;

import java.util.Optional;

@Mixin(BlockGetter.class)
public interface MixinBlockGetter extends PatchBlockGetter {
    
    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    private static BlockHitResult method_17746(ClipContext c) {
        Vec3 from = c.getFrom();
        Vec3 to = c.getTo();
        double toX = to.x;
        double toY = to.y;
        double toZ = to.z;
        double x = from.x - toX;
        double y = from.y - toY;
        double z = from.z - toZ;
        return PatchBlockHitResult.createMiss(toX, toY, toZ, Direction.getNearest(x, y, z), Mth.floor(toX), Mth.floor(toY), Mth.floor(toZ));
    }

    /**
     * @reason _
     * @author TheGreatWolf
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

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    default BlockHitResult clip(ClipContext context) {
        Vec3 from = context.getFrom();
        Vec3 to = context.getTo();
        if (from.equals(to)) {
            return method_17746(context);
        }
        double x0 = Mth.lerp(-1.0E-7, from.x, to.x);
        double y0 = Mth.lerp(-1.0E-7, from.y, to.y);
        double z0 = Mth.lerp(-1.0E-7, from.z, to.z);
        int x = Mth.floor(x0);
        int y = Mth.floor(y0);
        int z = Mth.floor(z0);
        BlockState blockState = this.getBlockState_(x, y, z);
        FluidState fluidState = this.getFluidState_(x, y, z);
        VoxelShape blockShape = context.getBlockShape_(blockState, (BlockGetter) this, x, y, z);
        BlockHitResult blockHitResult = this.clipWithInteractionOverride_(from, to, x, y, z, blockShape, blockState);
        VoxelShape fluidShape = context.getFluidShape_(fluidState, (BlockGetter) this, x, y, z);
        BlockHitResult fluidHitResult = fluidShape.clip_(from, to, x, y, z);
        double blockDist = blockHitResult == null ? Double.MAX_VALUE : from.distanceToSqr(blockHitResult.x(), blockHitResult.y(), blockHitResult.z());
        double fluidDist = fluidHitResult == null ? Double.MAX_VALUE : from.distanceToSqr(fluidHitResult.x(), fluidHitResult.y(), fluidHitResult.z());
        BlockHitResult hitResult = blockDist <= fluidDist ? blockHitResult : fluidHitResult;
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
        double dddx = ddx * (sx > 0 ? 1 - Mth.frac(x0) : Mth.frac(x0));
        double dddy = ddy * (sy > 0 ? 1 - Mth.frac(y0) : Mth.frac(y0));
        double dddz = ddz * (sz > 0 ? 1 - Mth.frac(z0) : Mth.frac(z0));
        while (true) {
            if (!(dddx <= 1) && !(dddy <= 1) && !(dddz <= 1)) {
                return method_17746(context);
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
            blockState = this.getBlockState_(x, y, z);
            fluidState = this.getFluidState_(x, y, z);
            blockShape = context.getBlockShape_(blockState, (BlockGetter) this, x, y, z);
            blockHitResult = this.clipWithInteractionOverride_(from, to, x, y, z, blockShape, blockState);
            fluidShape = context.getFluidShape_(fluidState, (BlockGetter) this, x, y, z);
            fluidHitResult = fluidShape.clip_(from, to, x, y, z);
            blockDist = blockHitResult == null ? Double.MAX_VALUE : from.distanceToSqr(blockHitResult.x(), blockHitResult.y(), blockHitResult.z());
            fluidDist = fluidHitResult == null ? Double.MAX_VALUE : from.distanceToSqr(fluidHitResult.x(), fluidHitResult.y(), fluidHitResult.z());
            hitResult = blockDist <= fluidDist ? blockHitResult : fluidHitResult;
            if (hitResult != null) {
                return hitResult;
            }
        }
    }

    @Unique
    default @Nullable BlockHitResult clipWithInteractionOverride_(Vec3 start, Vec3 end, int x, int y, int z, VoxelShape shape, BlockState state) {
        BlockHitResult shapeHitResult = shape.clip_(start, end, x, y, z);
        if (shapeHitResult != null) {
            BlockHitResult interactionHitResult = state.getInteractionShape_((BlockGetter) this, x, y, z).clip_(start, end, x, y, z);
            if (interactionHitResult != null) {
                Vec3 intLoc = interactionHitResult.getLocation();
                double intX = intLoc.x - start.x;
                double intY = intLoc.y - start.y;
                double intZ = intLoc.z - start.z;
                Vec3 shapeLoc = shapeHitResult.getLocation();
                double shapeX = shapeLoc.x - start.x;
                double shapeY = shapeLoc.y - start.y;
                double shapeZ = shapeLoc.z - start.z;
                if (intX * intX * intY * intY + intZ * intZ < shapeX * shapeX + shapeY * shapeY + shapeZ * shapeZ) {
                    return shapeHitResult.withDirection(interactionHitResult.getDirection());
                }
            }
        }
        return shapeHitResult;
    }

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
     * @reason _
     * @author TheGreatWolf
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
        if (context.isTargetBlock().test(state)) {
            return new BlockHitResult(to, Direction.getNearest(from.x - to.x, from.y - to.y, from.z - to.z), new BlockPos(to), false);
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
            if (context.isTargetBlock().test(state)) {
                return new BlockHitResult(to, Direction.getNearest(from.x - to.x, from.y - to.y, from.z - to.z), new BlockPos(to), false);
            }
        }
    }
}
