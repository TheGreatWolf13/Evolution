package tgw.evolution.patches;

public interface PatchLevelLightEngine {

    default int getRawBrightness_(long pos, int i) {
        throw new AbstractMethodError();
    }
}
