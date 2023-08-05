package tgw.evolution.patches;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public interface PatchBlockGetter {

    default <T extends BlockEntity> @Nullable T getBlockEntity_(int x, int y, int z, BlockEntityType<T> type) {
        BlockEntity te = this.getBlockEntity_(x, y, z);
        return te != null && te.getType() == type ? (T) te : null;
    }

    default @Nullable BlockEntity getBlockEntity_(BlockPos pos) {
        return this.getBlockEntity_(pos.getX(), pos.getY(), pos.getZ());
    }

    default @Nullable BlockEntity getBlockEntity_(int x, int y, int z) {
        throw new AbstractMethodError();
    }

    default BlockState getBlockStateAtSide(int x, int y, int z, Direction direction, int offset) {
        return switch (direction) {
            case WEST -> this.getBlockState_(x - offset, y, z);
            case EAST -> this.getBlockState_(x + offset, y, z);
            case DOWN -> this.getBlockState_(x, y - offset, z);
            case UP -> this.getBlockState_(x, y + offset, z);
            case NORTH -> this.getBlockState_(x, y, z - offset);
            case SOUTH -> this.getBlockState_(x, y, z + offset);
        };
    }

    default BlockState getBlockStateAtSide(int x, int y, int z, Direction direction) {
        return this.getBlockStateAtSide(x, y, z, direction, 1);
    }

    default BlockState getBlockStateAtSide(long pos, Direction dir) {
        return this.getBlockStateAtSide(BlockPos.getX(pos), BlockPos.getY(pos), BlockPos.getZ(pos), dir);
    }

    default BlockState getBlockState_(double x, double y, double z) {
        return this.getBlockState_(Mth.floor(x), Mth.floor(y), Mth.floor(z));
    }

    default BlockState getBlockState_(Vec3 vec) {
        return this.getBlockState_(vec.x, vec.y, vec.z);
    }

    default BlockState getBlockState_(BlockPos pos) {
        return this.getBlockState_(pos.getX(), pos.getY(), pos.getZ());
    }

    default BlockState getBlockState_(long pos) {
        return this.getBlockState_(BlockPos.getX(pos), BlockPos.getY(pos), BlockPos.getZ(pos));
    }

    default BlockState getBlockState_(int x, int y, int z) {
        throw new AbstractMethodError();
    }

    default FluidState getFluidStateAtSide(int x, int y, int z, Direction dir) {
        return switch (dir) {
            case WEST -> this.getFluidState_(x - 1, y, z);
            case EAST -> this.getFluidState_(x + 1, y, z);
            case DOWN -> this.getFluidState_(x, y - 1, z);
            case UP -> this.getFluidState_(x, y + 1, z);
            case NORTH -> this.getFluidState_(x, y, z - 1);
            case SOUTH -> this.getFluidState_(x, y, z + 1);
        };
    }

    default FluidState getFluidState_(BlockPos pos) {
        return this.getFluidState_(pos.getX(), pos.getY(), pos.getZ());
    }

    default FluidState getFluidState_(int x, int y, int z) {
        throw new AbstractMethodError();
    }

    default int getLightEmission_(int x, int y, int z) {
        return this.getBlockState_(x, y, z).getLightEmission();
    }
}
