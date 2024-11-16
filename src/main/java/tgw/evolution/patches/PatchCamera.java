package tgw.evolution.patches;

public interface PatchCamera {

    default int getXWrap() {
        throw new AbstractMethodError();
    }

    default int getZWrap() {
        throw new AbstractMethodError();
    }
}
