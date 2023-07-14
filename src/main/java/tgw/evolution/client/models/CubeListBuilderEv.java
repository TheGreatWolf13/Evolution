package tgw.evolution.client.models;

import net.minecraft.client.model.geom.builders.CubeDefinition;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import tgw.evolution.patches.PatchCubeDefinition;

public class CubeListBuilderEv extends CubeListBuilder {

    protected boolean shouldFix;

    public static CubeListBuilderEv create() {
        return new CubeListBuilderEv();
    }

    @Override
    public CubeListBuilderEv addBox(String comment,
                                    float originX,
                                    float originY,
                                    float originZ,
                                    int dimX,
                                    int dimY,
                                    int dimZ,
                                    CubeDeformation def,
                                    int xTexOffs,
                                    int yTexOffs) {
        this.texOffs(xTexOffs, yTexOffs);
        CubeDefinition c = new CubeDefinition(comment, this.xTexOffs, this.yTexOffs, originX, originY, originZ, dimX, dimY, dimZ, def,
                                              this.mirror, 1.0F, 1.0F);
        if (this.shouldFix) {
            ((PatchCubeDefinition) (Object) c).requestFix();
        }
        this.cubes.add(c);
        return this;
    }

    @Override
    public CubeListBuilderEv addBox(String comment,
                                    float originX,
                                    float originY,
                                    float originZ,
                                    int dimX,
                                    int dimY,
                                    int dimZ,
                                    int xTexOffs,
                                    int yTexOffs) {
        this.texOffs(xTexOffs, yTexOffs);
        CubeDefinition c = new CubeDefinition(comment, this.xTexOffs, this.yTexOffs, originX, originY, originZ, dimX, dimY, dimZ,
                                              CubeDeformation.NONE, this.mirror, 1.0F, 1.0F);
        if (this.shouldFix) {
            ((PatchCubeDefinition) (Object) c).requestFix();
        }
        this.cubes.add(c);
        return this;
    }

    @Override
    public CubeListBuilderEv addBox(float originX, float originY, float originZ, float dimX, float dimY, float dimZ) {
        CubeDefinition c = new CubeDefinition(null, this.xTexOffs, this.yTexOffs, originX, originY, originZ, dimX, dimY, dimZ, CubeDeformation.NONE,
                                              this.mirror, 1.0F, 1.0F);
        if (this.shouldFix) {
            ((PatchCubeDefinition) (Object) c).requestFix();
        }
        this.cubes.add(c);
        return this;
    }

    @Override
    public CubeListBuilderEv addBox(String comment,
                                    float originX,
                                    float originY,
                                    float originZ,
                                    float dimX,
                                    float dimY,
                                    float dimZ) {
        CubeDefinition c = new CubeDefinition(comment, this.xTexOffs, this.yTexOffs, originX, originY, originZ, dimX, dimY, dimZ,
                                              CubeDeformation.NONE, this.mirror, 1.0F, 1.0F);
        if (this.shouldFix) {
            ((PatchCubeDefinition) (Object) c).requestFix();
        }
        this.cubes.add(c);
        return this;
    }

    @Override
    public CubeListBuilderEv addBox(String comment,
                                    float originX,
                                    float originY,
                                    float originZ,
                                    float dimX,
                                    float dimY,
                                    float dimZ,
                                    CubeDeformation def) {
        CubeDefinition c = new CubeDefinition(comment, this.xTexOffs, this.yTexOffs, originX, originY, originZ, dimX, dimY, dimZ, def, this.mirror,
                                              1.0F, 1.0F);
        if (this.shouldFix) {
            ((PatchCubeDefinition) (Object) c).requestFix();
        }
        this.cubes.add(c);
        return this;
    }

    @Override
    public CubeListBuilderEv addBox(float originX,
                                    float originY,
                                    float originZ,
                                    float dimX,
                                    float dimY,
                                    float dimZ,
                                    boolean mirror) {
        CubeDefinition c = new CubeDefinition(null, this.xTexOffs, this.yTexOffs, originX, originY, originZ, dimX, dimY, dimZ, CubeDeformation.NONE,
                                              mirror, 1.0F, 1.0F);
        if (this.shouldFix) {
            ((PatchCubeDefinition) (Object) c).requestFix();
        }
        this.cubes.add(c);
        return this;
    }

    @Override
    public CubeListBuilderEv addBox(float originX,
                                    float originY,
                                    float originZ,
                                    float dimX,
                                    float dimY,
                                    float dimZ,
                                    CubeDeformation def,
                                    float texScaleU,
                                    float texScaleV) {
        CubeDefinition c = new CubeDefinition(null, this.xTexOffs, this.yTexOffs, originX, originY, originZ, dimX, dimY, dimZ, def, this.mirror,
                                              texScaleU, texScaleV);
        if (this.shouldFix) {
            ((PatchCubeDefinition) (Object) c).requestFix();
        }
        this.cubes.add(c);
        return this;
    }

    @Override
    public CubeListBuilderEv addBox(float originX,
                                    float originY,
                                    float originZ,
                                    float dimX,
                                    float dimY,
                                    float dimZ,
                                    CubeDeformation def) {
        CubeDefinition c = new CubeDefinition(null, this.xTexOffs, this.yTexOffs, originX, originY, originZ, dimX, dimY, dimZ, def, this.mirror, 1.0F,
                                              1.0F);
        if (this.shouldFix) {
            ((PatchCubeDefinition) (Object) c).requestFix();
        }
        this.cubes.add(c);
        return this;
    }

    public CubeListBuilderEv addBoxBend(float originX,
                                        float originY,
                                        float originZ,
                                        float dimX,
                                        float dimY,
                                        float dimZ,
                                        CubeDeformation def,
                                        boolean up) {
        CubeDefinition c = new CubeDefinition(null, this.xTexOffs, this.yTexOffs, originX, originY, originZ, dimX, dimY, dimZ, def, this.mirror, 1.0F,
                                              1.0F);
        ((PatchCubeDefinition) (Object) c).markBend(up);
        this.cubes.add(c);
        return this;
    }

    @Override
    public CubeListBuilderEv mirror() {
        return this.mirror(true);
    }

    @Override
    public CubeListBuilderEv mirror(boolean mirror) {
        this.mirror = mirror;
        return this;
    }

    public CubeListBuilderEv requestFix() {
        this.shouldFix = true;
        return this;
    }

    @Override
    public CubeListBuilderEv texOffs(int xTexOffs, int yTexOffs) {
        this.xTexOffs = xTexOffs;
        this.yTexOffs = yTexOffs;
        return this;
    }
}
