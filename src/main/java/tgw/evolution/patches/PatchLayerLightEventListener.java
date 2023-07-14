package tgw.evolution.patches;

public interface PatchLayerLightEventListener {

    default int getLightValue_(long pos) {
        throw new AbstractMethodError();
    }
}
