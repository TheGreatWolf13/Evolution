package tgw.evolution.client.renderer;

import tgw.evolution.util.collection.FArrayList;
import tgw.evolution.util.collection.FList;
import tgw.evolution.util.collection.IArrayList;
import tgw.evolution.util.collection.IList;

public final class RenderHelper {

    public static final float[] DEF_BRIGHTNESS = {1.0f, 1.0f, 1.0f, 1.0f};
    public static final ThreadLocal<float[]> BRIGHTNESS = ThreadLocal.withInitial(() -> new float[4]);
    public static final ThreadLocal<int[]> LIGHTMAP = ThreadLocal.withInitial(() -> new int[4]);
    public static final ThreadLocal<IList> INT_LIST = ThreadLocal.withInitial(IArrayList::new);
    public static final ThreadLocal<FList> FLOAT_LIST = ThreadLocal.withInitial(FArrayList::new);
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
