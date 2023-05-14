package tgw.evolution.util;

import net.minecraft.core.BlockPos;

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

    public void remove() {
        this.isPresent = false;
    }

    public void set(int x, int y, int z) {
        this.isPresent = true;
        this.pos.set(x, y, z);
    }
}
