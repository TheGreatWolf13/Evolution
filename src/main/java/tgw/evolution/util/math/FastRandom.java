package tgw.evolution.util.math;

import java.util.concurrent.atomic.AtomicLong;

/**
 * A basic implementation of {@link java.util.Random} without any atomic, volatile or synchronized attributes. Only safe to use on a single thread. Performs faster than the default {@link java.util.Random}.
 */
public class FastRandom implements IRandom {

    private static final AtomicLong SEED_UNIQUIFIER = new AtomicLong(8_682_522_807_148_012L);
    private boolean haveNextNextGaussian;
    private double nextNextGaussian;
    private long seed;

    public FastRandom() {
        this(seedUniquifier() ^ System.nanoTime());
    }

    public FastRandom(long seed) {
        this.setSeed(seed);
    }

    private static long initialScramble(long seed) {
        return (seed ^ 0x5_DEEC_E66DL) & (1L << 48) - 1;
    }

    private static long seedUniquifier() {
        // L'Ecuyer, "Tables of Linear Congruential Generators of
        // Different Sizes and Good Lattice Structure", 1999
        while (true) {
            long current = SEED_UNIQUIFIER.get();
            long next = current * 1_181_783_497_276_652_981L;
            if (SEED_UNIQUIFIER.compareAndSet(current, next)) {
                return next;
            }
        }
    }

    protected int next(int bits) {
        long nextSeed = this.seed * 0x5_DEEC_E66DL + 0xBL & (1L << 48) - 1;
        this.seed = nextSeed;
        return (int) (nextSeed >>> 48 - bits);
    }

    @Override
    public boolean nextBoolean() {
        return this.next(1) != 0;
    }

    @Override
    public void nextBytes(byte[] bytes) {
        for (int i = 0, len = bytes.length; i < len; ) {
            for (int rnd = this.nextInt(), n = Math.min(len - i, Integer.SIZE / Byte.SIZE); n-- > 0; rnd >>= Byte.SIZE) {
                bytes[i++] = (byte) rnd;
            }
        }
    }

    @Override
    public double nextDouble() {
        return (((long) this.next(26) << 27) + this.next(27)) * 0x1.0p-53;
    }

    @Override
    public float nextFloat() {
        return this.next(24) * 0x1.0p-24f;
    }

    @Override
    public double nextGaussian() {
        if (this.haveNextNextGaussian) {
            this.haveNextNextGaussian = false;
            return this.nextNextGaussian;
        }
        double v1 = 2 * this.nextDouble() - 1;
        double v2 = 2 * this.nextDouble() - 1;
        double s = v1 * v1 + v2 * v2;
        while (s >= 1 || s == 0) {
            v1 = 2 * this.nextDouble() - 1;
            v2 = 2 * this.nextDouble() - 1;
            s = v1 * v1 + v2 * v2;
        }
        double multiplier = StrictMath.sqrt(-2 * StrictMath.log(s) / s);
        this.nextNextGaussian = v2 * multiplier;
        this.haveNextNextGaussian = true;
        return v1 * multiplier;
    }

    @Override
    public int nextInt() {
        return this.next(32);
    }

    @Override
    public int nextInt(int bound) {
        if (bound <= 0) {
            throw new IllegalArgumentException("bound must be positive");
        }
        int r = this.next(31);
        int m = bound - 1;
        if ((bound & m) == 0) {
            // i.e., bound is a power of 2
            return (int) (bound * (long) r >> 31);
        }
        // reject over-represented candidates
        int u = r;
        while (u - (r = u % bound) + m < 0) {
            u = this.next(31);
        }
        return r;
    }

    @Override
    public long nextLong() {
        return ((long) this.next(32) << 32) + this.next(32);
    }

    @Override
    public void setSeed(long seed) {
        this.seed = initialScramble(seed);
        this.haveNextNextGaussian = false;
    }
}

