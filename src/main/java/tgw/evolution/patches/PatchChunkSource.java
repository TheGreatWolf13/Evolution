package tgw.evolution.patches;

public interface PatchChunkSource {

    default void onSectionEmptinessChanged(int secX, int secY, int secZ, boolean empty) {
        throw new AbstractMethodError();
    }
}
