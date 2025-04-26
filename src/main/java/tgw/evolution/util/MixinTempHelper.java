package tgw.evolution.util;

public final class MixinTempHelper {

    private MixinTempHelper() {
    }

    public static long asLong(int x, int y, int z) {
        return y & 0xf_ffffL | (z & 0x3f_ffffL) << 20 | (x & 0x3f_ffffL) << 42;
    }
}
