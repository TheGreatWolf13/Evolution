package tgw.evolution.patches;

import org.jetbrains.annotations.Nullable;

public interface PatchLayerLightEventListener {

    default int getClampledLightValue(long pos) {
        int value = this.getLightValue_(pos);
        int r = value & 0xF;
        int g = value >>> 5 & 0xF;
        int b = value >>> 10 & 0xF;
        return Math.max(r, Math.max(g, b));
    }

    default byte @Nullable [] getDataLayerData_(int secX, int secY, int secZ) {
        throw new AbstractMethodError();
    }

    default int getLightValue_(long pos) {
        throw new AbstractMethodError();
    }
}
