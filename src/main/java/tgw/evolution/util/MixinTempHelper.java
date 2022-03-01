package tgw.evolution.util;

public final class MixinTempHelper {

    private MixinTempHelper() {
    }

    public static float xRot(float progress) {
        return -progress * 2.25f;
    }

    public static float yRot(float progress, float headPitch) {
        return -headPitch * 2 / 3;
    }

    public static float zRot(float progress) {
        return 0.523_599f + 1.047_2f * progress;
    }
}
