package tgw.evolution.patches;

import net.minecraft.resources.ResourceLocation;
import tgw.evolution.client.util.Shader;

public interface PatchGameRenderer {

    default void loadShader(@Shader int shaderId, ResourceLocation resLoc) {
        throw new AbstractMethodError();
    }

    default void shutdownAllShaders() {
        throw new AbstractMethodError();
    }

    default void shutdownShader(@Shader int shaderId) {
        throw new AbstractMethodError();
    }
}
