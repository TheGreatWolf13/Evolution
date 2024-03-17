package tgw.evolution.blocks;

import tgw.evolution.util.math.PropagationDirection;

public interface IFillable extends IPhysics {

    static int getIntegrity(int packed) {
        return packed & 0xFF;
    }

    static int getLoadFactor(int packed) {
        return packed >> 16 & 0xFF;
    }

    static boolean getStable(int packed) {
        return (packed & 1 << 31) != 0;
    }

    static int getStableLoadFactor(int packed) {
        if (getStable(packed)) {
            return 0;
        }
        return getLoadFactor(packed);
    }

    static int packInternalPos(int x, int y, int z, int failure, PropagationDirection propDir, boolean wasFillable) {
        assert 0 <= x && x < 16 : "X out of bounds: " + x;
        assert 0 <= z && z < 16 : "Z out of bounds: " + z;
        assert 0 <= failure && failure < 256 : "Failure out of bounds: " + failure;
        int pos = z << 4 | x;
        pos |= failure << 8;
        pos |= y << 21;
        if (wasFillable) {
            pos |= 1 << 16;
        }
        pos |= propDir.ordinal() << 17;
        return pos;
    }

    static int unpackFailure(int packed) {
        return packed >> 8 & 0xFF;
    }

    static PropagationDirection unpackPropDir(int packed) {
        return PropagationDirection.VALUES[packed >> 17 & 0b1111];
    }

    static boolean unpackWasFillable(int packed) {
        return (packed & 1 << 16) != 0;
    }

    static int unpackX(int packed) {
        return packed & 0xF;
    }

    static int unpackY(int packed) {
        return packed >> 21;
    }

    static int unpackZ(int packed) {
        return packed >> 4 & 0xF;
    }
}
