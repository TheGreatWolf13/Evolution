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

    public boolean isSame(BlockPos pos, Direction offset, @Nullable Direction moving, boolean tolerance) {
        if (!this.isPresent()) {
            return false;
        }
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        switch (offset) {
            case UP -> ++y;
            case DOWN -> --y;
            case WEST -> --x;
            case EAST -> ++x;
            case NORTH -> --z;
            case SOUTH -> ++z;
        }
        int px = this.pos.getX();
        int py = this.pos.getY();
        int pz = this.pos.getZ();
        int tx = px;
        int ty = py;
        int tz = pz;
        if (moving != null) {
            switch (moving) {
                case DOWN -> {
                    --py;
                    ty -= 2;
                }
                case UP -> {
                    ++py;
                    ty += 2;
                }
                case WEST -> {
                    --px;
                    tx -= 2;
                }
                case EAST -> {
                    ++px;
                    tx += 2;
                }
                case NORTH -> {
                    --pz;
                    tz -= 2;
                }
                case SOUTH -> {
                    ++pz;
                    tz += 2;
                }
            }
        }
        if (px == x && py == y && pz == z) {
            return true;
        }
        if (moving == null || !tolerance) {
            return false;
        }
        return tx == x && ty == y && tz == z;
    }

    public void remove() {
        this.isPresent = false;
    }

    public void set(int x, int y, int z) {
        this.isPresent = true;
        this.pos.set(x, y, z);
    }

    public void setWithOffset(BlockPos pos, Direction offset) {
        this.isPresent = true;
        this.pos.setWithOffset(pos, offset);
    }
}
