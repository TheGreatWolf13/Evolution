package tgw.evolution.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class CloneBlockInfo {
    public final BlockState state;
    public final @Nullable CompoundTag tag;
    public final int x;
    public final int y;
    public final int z;

    public CloneBlockInfo(int x, int y, int z, BlockState state, @Nullable CompoundTag tag) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.state = state;
        this.tag = tag;
    }
}
