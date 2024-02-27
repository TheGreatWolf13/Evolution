package tgw.evolution.patches;

import net.minecraft.core.SectionPos;

public interface PatchLightEventListener {

    default void checkBlock_(long pos) {
        throw new AbstractMethodError();
    }

    default void enableLightSources_(int secX, int secZ, boolean bl) {
        throw new AbstractMethodError();
    }

    default void onBlockEmissionIncrease_(long pos, int lightEmission) {
        throw new AbstractMethodError();
    }

    default void updateSectionStatus_block(int x, int y, int z, boolean notReady) {
        this.updateSectionStatus_sec(SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(y), SectionPos.blockToSectionCoord(z), notReady);
    }

    default void updateSectionStatus_sec(int secX, int secY, int secZ, boolean notReady) {
        throw new AbstractMethodError();
    }
}
