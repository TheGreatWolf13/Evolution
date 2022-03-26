package tgw.evolution.client.renderer;

import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;

public final class RenderHelper {

    public static final float[] DEF_BRIGHTNESS = {1.0f, 1.0f, 1.0f, 1.0f};
    public static final ThreadLocal<float[]> BRIGHTNESS = ThreadLocal.withInitial(() -> new float[4]);
    public static final ThreadLocal<int[]> LIGHTMAP = ThreadLocal.withInitial(() -> new int[4]);
    public static final ThreadLocal<IntArrayList> INT_LIST = ThreadLocal.withInitial(IntArrayList::new);
    public static final ThreadLocal<FloatArrayList> FLOAT_LIST = ThreadLocal.withInitial(FloatArrayList::new);
    public static final String[] SAMPLER_NAMES = {"Sampler0",
                                                  "Sampler1",
                                                  "Sampler2",
                                                  "Sampler3",
                                                  "Sampler4",
                                                  "Sampler5",
                                                  "Sampler6",
                                                  "Sampler7",
                                                  "Sampler8",
                                                  "Sampler9",
                                                  "Sampler10",
                                                  "Sampler11"};

    private RenderHelper() {
    }
}
