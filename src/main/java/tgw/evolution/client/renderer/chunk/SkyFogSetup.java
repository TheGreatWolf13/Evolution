package tgw.evolution.client.renderer.chunk;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.FogRenderer;

public class SkyFogSetup {

    private Camera camera;
    private float farPlaneDist;
    private boolean nearFog;
    private float partialTicks;

    public SkyFogSetup set(Camera camera, float farPlaneDist, boolean nearFog, float partialTicks) {
        this.camera = camera;
        this.farPlaneDist = farPlaneDist;
        this.nearFog = nearFog;
        this.partialTicks = partialTicks;
        return this;
    }

    public void setup() {
        FogRenderer.setupFog(this.camera, FogRenderer.FogMode.FOG_SKY, this.farPlaneDist, this.nearFog, this.partialTicks);
    }
}
