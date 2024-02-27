package tgw.evolution.patches;

import org.jetbrains.annotations.Nullable;

public interface PatchLayerLightEventListener {

    default byte @Nullable [] getDataLayerData_(int secX, int secY, int secZ) {
        throw new AbstractMethodError();
    }

    default int getLightValue_(long pos) {
        throw new AbstractMethodError();
    }
}
