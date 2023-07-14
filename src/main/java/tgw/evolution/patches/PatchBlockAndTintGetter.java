package tgw.evolution.patches;

import net.minecraft.world.level.LightLayer;

public interface PatchBlockAndTintGetter {

    default int getBrightness_(LightLayer lightLayer, long pos) {
        throw new AbstractMethodError();
    }

    default int getRawBrightness_(long pos, int i) {
        throw new AbstractMethodError();
    }
}
