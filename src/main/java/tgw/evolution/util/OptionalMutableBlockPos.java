package tgw.evolution.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

public class OptionalMutableBlockPos {

    private final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
    private boolean isPresent;

    public BlockPos get() {
        if (!this.isPresent) {
            throw new NullPointerException("Optional is not present!");
        }
        return this.pos;
    }

    public boolean isPresent() {
        return this.isPresent;
    }

    public boolean isSame(int x, int y, int z, Direction offset, @Nullable Direction moving) {
        if (moving == null) {
            return false;
        }
        if (!this.isPresent()) {
            return false;
        }
        int ox = x;
        int oy = y;
        int oz = z;
        switch (offset) {
            case UP -> ++oy;
            case DOWN -> --oy;
            case WEST -> --ox;
            case EAST -> ++ox;
            case NORTH -> --oz;
            case SOUTH -> ++oz;
        }
        int px = this.pos.getX();
        int py = this.pos.getY();
        int pz = this.pos.getZ();
        switch (moving) {
            case DOWN -> --py;
            case UP -> ++py;
            case WEST -> --px;
            case EAST -> ++px;
            case NORTH -> --pz;
            case SOUTH -> ++pz;
        }
        return px == ox && py == oy && pz == oz || moving == offset && this.pos.getX() == ox && this.pos.getY() == oy && this.pos.getZ() == oz;
    }

    public void remove() {
        this.isPresent = false;
    }

    public void set(int x, int y, int z) {
        this.isPresent = true;
        this.pos.set(x, y, z);
    }

    public void setWithOffset(int x, int y, int z, Direction offset) {
        this.isPresent = true;
        this.pos.set(x, y, z).move(offset);
    }
}
