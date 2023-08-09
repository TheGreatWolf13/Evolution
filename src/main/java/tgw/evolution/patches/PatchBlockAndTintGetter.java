package tgw.evolution.patches;

import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.LightLayer;

public interface PatchBlockAndTintGetter {

    default boolean canSeeSky_(long pos) {
        throw new AbstractMethodError();
    }

    default int getBlockTint_(int x, int y, int z, ColorResolver colorResolver) {
        throw new AbstractMethodError();
    }

    default int getBrightness_(LightLayer lightLayer, long pos) {
        throw new AbstractMethodError();
    }

    default int getRawBrightness_(long pos, int skyDarken) {
        throw new AbstractMethodError();
    }
}
