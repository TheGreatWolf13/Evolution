package tgw.evolution.patches;

import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.DataLayer;
import org.jetbrains.annotations.Nullable;

public interface PatchLevelLightEngine {

    default int getRawBrightness_(long pos, int i) {
        throw new AbstractMethodError();
    }

    default void queueSectionData_(LightLayer lightLayer, int secX, int secY, int secZ, @Nullable DataLayer dataLayer, boolean bl) {
        throw new AbstractMethodError();
    }
}
