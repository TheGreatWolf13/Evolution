package tgw.evolution.patches;

public interface ICubePatch {

    void fixInit(int u, int v, float dimX, float dimY, float dimZ, float growX, float growY, float growZ, boolean mirror, float width, float height);

    void fixInitBend(int u,
                     int v,
                     float dimX,
                     float dimY,
                     float dimZ,
                     float growX,
                     float growY,
                     float growZ,
                     boolean mirror,
                     float width,
                     float height,
                     boolean up);
}
