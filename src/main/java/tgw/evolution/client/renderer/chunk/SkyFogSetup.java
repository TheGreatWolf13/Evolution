package tgw.evolution.client.renderer.chunk;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.FogRenderer;

public class SkyFogSetup {

    private Camera camera;
    private float farPlaneDist;
    private boolean nearFog;

    public SkyFogSetup set(Camera camera, float farPlaneDist, boolean nearFog) {
        this.camera = camera;
        this.farPlaneDist = farPlaneDist;
        this.nearFog = nearFog;
        return this;
    }

    public void setup() {
        FogRenderer.setupFog(this.camera, FogRenderer.FogMode.FOG_SKY, this.farPlaneDist, this.nearFog);
    }
}
