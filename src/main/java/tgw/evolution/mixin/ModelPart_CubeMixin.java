package tgw.evolution.mixin;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.patches.ICubePatch;

@SuppressWarnings("UnnecessaryLocalVariable")
@Mixin(ModelPart.Cube.class)
public abstract class ModelPart_CubeMixin implements ICubePatch {

    @Final
    @Shadow
    public float maxX;
    @Final
    @Shadow
    public float maxY;
    @Final
    @Shadow
    public float maxZ;
    @Final
    @Shadow
    public float minX;
    @Final
    @Shadow
    public float minY;
    @Final
    @Shadow
    public float minZ;
    @Shadow
    public ModelPart.Polygon[] polygons;

    @Override
    public void fixInit(int u,
                        int v,
                        float dimX,
                        float dimY,
                        float dimZ,
                        float growX,
                        float growY,
                        float growZ,
                        boolean mirror,
                        float width,
                        float height) {
        float minX = this.minX;
        float minY = this.minY;
        float minZ = this.minZ;
        float maxX = this.maxX;
        float maxY = this.maxY;
        float maxZ = this.maxZ;
        minX -= growX;
        minY -= growY;
        minZ -= growZ;
        maxX += growX;
        maxY += growY;
        maxZ += growZ;
        if (mirror) {
            float t = maxX;
            maxX = minX;
            minX = t;
        }
        ModelPart.Vertex v000 = new ModelPart.Vertex(maxX, maxY, minZ, 0.0F, 0.0F);
        ModelPart.Vertex v001 = new ModelPart.Vertex(maxX, maxY, maxZ, 0.0F, 0.0F);
        ModelPart.Vertex v010 = new ModelPart.Vertex(maxX, minY, minZ, 8.0F, 0.0F);
        ModelPart.Vertex v011 = new ModelPart.Vertex(maxX, minY, maxZ, 8.0F, 0.0F);
        ModelPart.Vertex v100 = new ModelPart.Vertex(minX, maxY, minZ, 0.0F, 8.0F);
        ModelPart.Vertex v101 = new ModelPart.Vertex(minX, maxY, maxZ, 0.0F, 8.0F);
        ModelPart.Vertex v110 = new ModelPart.Vertex(minX, minY, minZ, 8.0F, 8.0F);
        ModelPart.Vertex v111 = new ModelPart.Vertex(minX, minY, maxZ, 8.0F, 8.0F);
        float x0 = u;
        float x1 = u + dimZ;
        float x2 = u + dimZ + dimX;
        float x3Up = u + dimZ + dimX + dimX;
        float x3 = u + dimZ + dimX + dimZ;
        float x4 = u + dimZ + dimX + dimZ + dimX;
        float y0 = v;
        float y1 = v + dimZ;
        float y2 = v + dimZ + dimY;
        this.polygons[2] = new ModelPart.Polygon(new ModelPart.Vertex[]{v101, v001, v000, v100},
                                                 x1, y0, x2, y1, width, height, mirror, Direction.DOWN);
        this.polygons[3] = new ModelPart.Polygon(new ModelPart.Vertex[]{v110, v010, v011, v111},
                                                 x2, y1, x3Up, y0, width, height, mirror, Direction.UP);
        this.polygons[1] = new ModelPart.Polygon(new ModelPart.Vertex[]{v000, v001, v011, v010},
                                                 x0, y1, x1, y2, width, height, mirror, Direction.WEST);
        this.polygons[4] = new ModelPart.Polygon(new ModelPart.Vertex[]{v100, v000, v010, v110},
                                                 x1, y1, x2, y2, width, height, mirror, Direction.NORTH);
        this.polygons[0] = new ModelPart.Polygon(new ModelPart.Vertex[]{v101, v100, v110, v111},
                                                 x2, y1, x3, y2, width, height, mirror, Direction.EAST);
        this.polygons[5] = new ModelPart.Polygon(new ModelPart.Vertex[]{v001, v101, v111, v011},
                                                 x3, y1, x4, y2, width, height, mirror, Direction.SOUTH);
    }

    @Override
    public void fixInitBend(int u,
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
                            boolean up) {
        float minX = this.minX;
        float minY = this.minY;
        float minZ = this.minZ;
        float maxX = this.maxX;
        float maxY = this.maxY;
        float maxZ = this.maxZ;
        minX -= growX;
        minY -= growY;
        minZ -= growZ;
        maxX += growX;
        maxY += growY;
        maxZ += growZ;
        if (mirror) {
            float t = maxX;
            maxX = minX;
            minX = t;
        }
        ModelPart.Vertex v000 = new ModelPart.Vertex(maxX, maxY, minZ, 0.0F, 0.0F);
        ModelPart.Vertex v001 = new ModelPart.Vertex(maxX, maxY, maxZ, 0.0F, 0.0F);
        ModelPart.Vertex v010 = new ModelPart.Vertex(maxX, minY, minZ, 8.0F, 0.0F);
        ModelPart.Vertex v011 = new ModelPart.Vertex(maxX, minY, maxZ, 8.0F, 0.0F);
        ModelPart.Vertex v100 = new ModelPart.Vertex(minX, maxY, minZ, 0.0F, 8.0F);
        ModelPart.Vertex v101 = new ModelPart.Vertex(minX, maxY, maxZ, 0.0F, 8.0F);
        ModelPart.Vertex v110 = new ModelPart.Vertex(minX, minY, minZ, 8.0F, 8.0F);
        ModelPart.Vertex v111 = new ModelPart.Vertex(minX, minY, maxZ, 8.0F, 8.0F);
        float x0 = u;
        float x1 = u + dimZ;
        float x2 = u + dimZ + dimX;
        float x3Up = u + dimZ + dimX + dimX;
        float x3 = u + dimZ + dimX + dimZ;
        float x4 = u + dimZ + dimX + dimZ + dimX;
        float y0 = v;
        float y1 = v + dimZ;
        float y2 = v + dimZ + dimY;
        if (up) {
            this.polygons[2] = new ModelPart.Polygon(new ModelPart.Vertex[]{v101, v001, v000, v100},
                                                     x1, y0, x2, y1, width, height, mirror, Direction.DOWN);
            this.polygons[3] = new ModelPart.Polygon(new ModelPart.Vertex[]{v110, v010, v011, v111},
                                                     x2, y2 - 1, x3Up, y2, width, height, mirror, Direction.UP);
        }
        else {
            this.polygons[2] = new ModelPart.Polygon(new ModelPart.Vertex[]{v101, v001, v000, v100},
                                                     x1, y1 + 1, x2, y1, width, height, mirror, Direction.DOWN);
            this.polygons[3] = new ModelPart.Polygon(new ModelPart.Vertex[]{v110, v010, v011, v111},
                                                     x2, y1 - dimY, x3Up, y0 - dimY, width, height, mirror, Direction.UP);
        }
        this.polygons[1] = new ModelPart.Polygon(new ModelPart.Vertex[]{v000, v001, v011, v010},
                                                 x0, y1, x1, y2, width, height, mirror, Direction.WEST);
        this.polygons[4] = new ModelPart.Polygon(new ModelPart.Vertex[]{v100, v000, v010, v110},
                                                 x1, y1, x2, y2, width, height, mirror, Direction.NORTH);
        this.polygons[0] = new ModelPart.Polygon(new ModelPart.Vertex[]{v101, v100, v110, v111},
                                                 x2, y1, x3, y2, width, height, mirror, Direction.EAST);
        this.polygons[5] = new ModelPart.Polygon(new ModelPart.Vertex[]{v001, v101, v111, v011},
                                                 x3, y1, x4, y2, width, height, mirror, Direction.SOUTH);
    }
}
