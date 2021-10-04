package tgw.evolution.util;

import it.unimi.dsi.fastutil.HashCommon;

// SplitMixRandom implementation from DSI Utilities, adopted in a minimal implementation to not
// import Apache Commons.
//
// http://xoshiro.di.unimi.it/
public class SplitMixRandom {

    private static final long PHI = 0x9E37_79B9_7F4A_7C15L;
    private long x;

    public SplitMixRandom(final long seed) {
        this.setSeed(seed);
    }

    private static long staffordMix13(long z) {
        z = (z ^ z >>> 30) * 0xBF58_476D_1CE4_E5B9L;
        z = (z ^ z >>> 27) * 0x94D0_49BB_1331_11EBL;
        return z ^ z >>> 31;
    }

    public long nextLong() {
        return staffordMix13(this.x += PHI);
    }

    public void setSeed(final long seed) {
        this.x = HashCommon.murmurHash3(seed);
    }

    public void setState(final long state) {
        this.x = state;
    }
}
