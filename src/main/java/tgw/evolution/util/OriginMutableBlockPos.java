package tgw.evolution.util;

import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.Mutable;

public class OriginMutableBlockPos {
    private final Mutable pos;
    private int x;
    private int x0;
    private int y;
    private int y0;
    private int z;
    private int z0;

    public OriginMutableBlockPos(int x0, int y0, int z0) {
        this.x0 = x0;
        this.y0 = y0;
        this.z0 = z0;
        this.x = x0;
        this.y = y0;
        this.z = z0;
        this.pos = new Mutable(x0, y0, z0);
    }

    public OriginMutableBlockPos(BlockPos pos) {
        this.x0 = pos.getX();
        this.y0 = pos.getY();
        this.z0 = pos.getZ();
        this.x = this.x0;
        this.y = this.y0;
        this.z = this.z0;
        this.pos = new Mutable(this.x, this.y, this.z);
    }

    public OriginMutableBlockPos add(int deltaX, int deltaY, int deltaZ) {
        this.x += deltaX;
        this.y += deltaY;
        this.z += deltaZ;
        return this;
    }

    public OriginMutableBlockPos down() {
        return this.down(1);
    }

    public OriginMutableBlockPos down(int n) {
        this.y -= n;
        return this;
    }

    public OriginMutableBlockPos east() {
        return this.east(1);
    }

    public OriginMutableBlockPos east(int n) {
        this.x += n;
        return this;
    }

    public Mutable getPos() {
        return this.pos.set(this.x, this.y, this.z);
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getZ() {
        return this.z;
    }

    public OriginMutableBlockPos north() {
        return this.north(1);
    }

    public OriginMutableBlockPos north(int n) {
        this.z -= n;
        return this;
    }

    public OriginMutableBlockPos offset(Direction dir, int n) {
        switch (dir) {
            case DOWN:
                this.y -= n;
                return this;
            case EAST:
                this.x += n;
                return this;
            case NORTH:
                this.z -= n;
                return this;
            case SOUTH:
                this.z += n;
                return this;
            case UP:
                this.y += n;
                return this;
            case WEST:
                this.x -= n;
                return this;
        }
        throw new IllegalStateException("Cannot determine Direction for " + dir);
    }

    public OriginMutableBlockPos offset(Direction dir) {
        return this.offset(dir, 1);
    }

    /**
     * Resets the OriginMutableBlockPos to its origin.
     */
    public OriginMutableBlockPos reset() {
        this.x = this.x0;
        this.y = this.y0;
        this.z = this.z0;
        return this;
    }

    public void setOrigin(int x0, int y0, int z0) {
        this.x0 = x0;
        this.y0 = y0;
        this.z0 = z0;
    }

    public void setPos(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public OriginMutableBlockPos south() {
        return this.south(1);
    }

    public OriginMutableBlockPos south(int n) {
        this.z += n;
        return this;
    }

    public OriginMutableBlockPos up() {
        return this.up(1);
    }

    public OriginMutableBlockPos up(int n) {
        this.y += n;
        return this;
    }

    public OriginMutableBlockPos west() {
        return this.west(1);
    }

    public OriginMutableBlockPos west(int n) {
        this.x -= n;
        return this;
    }
}
