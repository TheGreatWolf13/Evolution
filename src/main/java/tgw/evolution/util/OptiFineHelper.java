package tgw.evolution.util;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class OptiFineHelper {
    private static Boolean loaded;

    private OptiFineHelper() {
    }

    public static boolean isLoaded() {
        if (loaded == null) {
            try {
                Class.forName("optifine.Installer");
                loaded = true;
            }
            catch (ClassNotFoundException e) {
                loaded = false;
            }
        }
        return loaded;
    }
}
