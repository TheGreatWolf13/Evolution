package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(BlockPos.class)
public abstract class BlockPosMixin extends Vec3i {

    public BlockPosMixin(double pX, double pY, double pZ) {
        super(pX, pY, pZ);
    }

    /**
     * @author JellySquid
     * <p>
     * Simplify and inline
     */
    @Override
    @Overwrite
    public BlockPos above() {
        return new BlockPos(this.getX(), this.getY() + 1, this.getZ());
    }

    /**
     * @author JellySquid
     * <p>
     * Simplify and inline
     */
    @Override
    @Overwrite
    public BlockPos above(int distance) {
        return new BlockPos(this.getX(), this.getY() + distance, this.getZ());
    }

    /**
     * @author JellySquid
     * <p>
     * Simplify and inline
     */
    @Override
    @Overwrite
    public BlockPos below() {
        return new BlockPos(this.getX(), this.getY() - 1, this.getZ());
    }

    /**
     * @author JellySquid
     * <p>
     * Simplify and inline
     */
    @Override
    @Overwrite
    public BlockPos below(int distance) {
        return new BlockPos(this.getX(), this.getY() - distance, this.getZ());
    }

    /**
     * @author JellySquid
     * <p>
     * Simplify and inline
     */
    @Override
    @Overwrite
    public BlockPos east() {
        return new BlockPos(this.getX() + 1, this.getY(), this.getZ());
    }

    /**
     * @author JellySquid
     * <p>
     * Simplify and inline
     */
    @Override
    @Overwrite
    public BlockPos east(int distance) {
        return new BlockPos(this.getX() + distance, this.getY(), this.getZ());
    }

    /**
     * @author JellySquid
     * <p>
     * Simplify and inline
     */
    @Override
    @Overwrite
    public BlockPos north() {
        return new BlockPos(this.getX(), this.getY(), this.getZ() - 1);
    }

    /**
     * @author JellySquid
     * <p>
     * Simplify and inline
     */
    @Override
    @Overwrite
    public BlockPos north(int distance) {
        return new BlockPos(this.getX(), this.getY(), this.getZ() - distance);
    }

    /**
     * @author JellySquid
     * <p>
     * Simplify and inline
     */
    @Override
    @Overwrite
    public BlockPos south() {
        return new BlockPos(this.getX(), this.getY(), this.getZ() + 1);
    }

    /**
     * @author JellySquid
     * <p>
     * Simplify and inline
     */
    @Override
    @Overwrite
    public BlockPos south(int distance) {
        return new BlockPos(this.getX(), this.getY(), this.getZ() + distance);
    }

    /**
     * @author JellySquid
     * <p>
     * Simplify and inline
     */
    @Override
    @Overwrite
    public BlockPos west() {
        return new BlockPos(this.getX() - 1, this.getY(), this.getZ());
    }

    /**
     * @author JellySquid
     * <p>
     * Simplify and inline
     */
    @Override
    @Overwrite
    public BlockPos west(int distance) {
        return new BlockPos(this.getX() - distance, this.getY(), this.getZ());
    }
}
