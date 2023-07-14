package tgw.evolution.blocks;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Range;

public interface IAir {

    /**
     * Represents a position within a {@link net.minecraft.world.level.chunk.LevelChunk}.<br>
     * Bit 0 ~ 3: x <br>
     * Bit 4 ~ 7: z <br>
     * Bit 8: forceUpdate <br>
     * Bit 9 ~ 32: y <br>
     */
    static int packInternalPos(int x, int y, int z, boolean forceUpdate) {
        assert 0 <= x && x <= 15 : "X out of bounds: " + x;
        assert 0 <= z && z <= 15 : "Z out of bounds: " + z;
        int pos = z << 4 | x;
        pos |= y << 9;
        if (forceUpdate) {
            pos |= 1 << 8;
        }
        return pos;
    }

    static int packInternalPos(int x, int y, int z) {
        return packInternalPos(x, y, z, false);
    }

    static boolean unpackForceUpdate(int packed) {
        return (packed & 1 << 8) != 0;
    }

    static int unpackX(int packed) {
        return packed & 0b1111;
    }

    static int unpackY(int packed) {
        return packed >> 9;
    }

    static int unpackZ(int packed) {
        return packed >> 4 & 0b1111;
    }

    boolean allowsFrom(BlockState state, Direction from);

    @Range(from = 1, to = 31) int increment(BlockState state, Direction from);
}
