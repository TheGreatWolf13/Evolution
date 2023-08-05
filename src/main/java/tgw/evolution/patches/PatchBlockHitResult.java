package tgw.evolution.patches;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public interface PatchBlockHitResult {

    static BlockHitResult create(double x, double y, double z, Direction direction, int posX, int posY, int posZ, boolean inside) {
        BlockHitResult hitResult = new BlockHitResult(false, Vec3.ZERO, direction, BlockPos.ZERO, inside);
        hitResult.set(x, y, z);
        hitResult.setPos(posX, posY, posZ);
        return hitResult;
    }

    static BlockHitResult createMiss(double x, double y, double z, Direction direction, int posX, int posY, int posZ) {
        BlockHitResult hitResult = new BlockHitResult(true, Vec3.ZERO, direction, BlockPos.ZERO, false);
        hitResult.set(x, y, z);
        hitResult.setPos(posX, posY, posZ);
        return hitResult;
    }

    default int posX() {
        throw new AbstractMethodError();
    }

    default int posY() {
        throw new AbstractMethodError();
    }

    default int posZ() {
        throw new AbstractMethodError();
    }

    default void setPos(int x, int y, int z) {
        throw new AbstractMethodError();
    }

    default BlockHitResult withPosition_(int x, int y, int z) {
        throw new AbstractMethodError();
    }
}
