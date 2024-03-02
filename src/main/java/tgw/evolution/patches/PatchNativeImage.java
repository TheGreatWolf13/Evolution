package tgw.evolution.patches;

import org.jetbrains.annotations.Nullable;

import java.nio.IntBuffer;

public interface PatchNativeImage {

    default @Nullable IntBuffer getBuffer() {
        throw new AbstractMethodError();
    }
}
