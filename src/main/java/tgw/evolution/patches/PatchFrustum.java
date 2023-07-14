package tgw.evolution.patches;

import tgw.evolution.client.renderer.chunk.Visibility;

public interface PatchFrustum {

    default @Visibility int intersectWith(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        throw new AbstractMethodError();
    }
}
