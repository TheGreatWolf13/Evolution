package tgw.evolution.util.constants;

import org.jetbrains.annotations.Nullable;

public final class OptiFineHelper {
    private static @Nullable Boolean loaded;

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
