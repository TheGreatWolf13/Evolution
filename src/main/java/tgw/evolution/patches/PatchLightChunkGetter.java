package tgw.evolution.patches;

import net.minecraft.world.level.LightLayer;

public interface PatchLightChunkGetter {

    default void onLightUpdate_(LightLayer lightLayer, int secX, int secY, int secZ) {
        throw new AbstractMethodError();
    }
}
