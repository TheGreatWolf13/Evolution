package tgw.evolution.patches;

import net.minecraft.resources.ResourceLocation;

public interface IGameRendererPatch {

    void loadShader(int shaderId, ResourceLocation resLoc);

    void shutdownAllShaders();

    void shutdownShader(int shaderId);
}
