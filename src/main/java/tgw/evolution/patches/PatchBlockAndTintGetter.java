package tgw.evolution.patches;

public interface PatchBlockAndTintGetter {

    default int getRawBrightness_(long pos, int i) {
        throw new AbstractMethodError();
    }
}
