package tgw.evolution.patches;

import net.minecraft.resources.ResourceLocation;

public interface PatchGameRenderer {

    void loadShader(int shaderId, ResourceLocation resLoc);

    void shutdownAllShaders();

    void shutdownShader(int shaderId);
}
