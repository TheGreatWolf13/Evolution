package tgw.evolution.client.models.tile;

import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.client.renderer.model.Model;
import tgw.evolution.util.MathHelper;

public class ModelTilePitKiln extends Model {

    private final RendererModel strawTopLayer;
    private final RendererModel strawWestLayer1;
    private final RendererModel strawWestLayer2;
    private final RendererModel strawWestLayer3;
    private final RendererModel strawWestLayer4;
    private final RendererModel strawWestLayer5;
    private final RendererModel strawWestLayer6;
    private final RendererModel strawWestLayer7;
    private final RendererModel strawWestLayer8;
    private final RendererModel[] westLayer;
    private final RendererModel strawNorthLayer1;
    private final RendererModel strawNorthLayer2;
    private final RendererModel strawNorthLayer3;
    private final RendererModel strawNorthLayer4;
    private final RendererModel strawNorthLayer5;
    private final RendererModel strawNorthLayer6;
    private final RendererModel strawNorthLayer7;
    private final RendererModel strawNorthLayer8;
    private final RendererModel[] northLayer;
    private final RendererModel strawSouthLayer1;
    private final RendererModel strawSouthLayer2;
    private final RendererModel strawSouthLayer3;
    private final RendererModel strawSouthLayer4;
    private final RendererModel strawSouthLayer5;
    private final RendererModel strawSouthLayer6;
    private final RendererModel strawSouthLayer7;
    private final RendererModel strawSouthLayer8;
    private final RendererModel[] southLayer;
    private final RendererModel strawEastLayer1;
    private final RendererModel strawEastLayer2;
    private final RendererModel strawEastLayer3;
    private final RendererModel strawEastLayer4;
    private final RendererModel strawEastLayer5;
    private final RendererModel strawEastLayer6;
    private final RendererModel strawEastLayer7;
    private final RendererModel strawEastLayer8;
    private final RendererModel[] eastLayer;

    public ModelTilePitKiln() {
        this.textureHeight = 24;
        this.textureWidth = 32;
        this.strawTopLayer = new RendererModel(this, -16, 0);
        this.strawTopLayer.addBox(0, 0, 0, 16, 0, 16);
        this.strawWestLayer1 = new RendererModel(this, 0, 0);
        this.strawWestLayer1.addBox(0, 0, 0, 0, 1, 16);
        this.strawWestLayer2 = new RendererModel(this, 0, 0);
        this.strawWestLayer2.addBox(0, 0, 0, 0, 2, 16);
        this.strawWestLayer3 = new RendererModel(this, 0, 0);
        this.strawWestLayer3.addBox(0, 0, 0, 0, 3, 16);
        this.strawWestLayer4 = new RendererModel(this, 0, 0);
        this.strawWestLayer4.addBox(0, 0, 0, 0, 4, 16);
        this.strawWestLayer5 = new RendererModel(this, 0, 0);
        this.strawWestLayer5.addBox(0, 0, 0, 0, 5, 16);
        this.strawWestLayer6 = new RendererModel(this, 0, 0);
        this.strawWestLayer6.addBox(0, 0, 0, 0, 6, 16);
        this.strawWestLayer7 = new RendererModel(this, 0, 0);
        this.strawWestLayer7.addBox(0, 0, 0, 0, 7, 16);
        this.strawWestLayer8 = new RendererModel(this, 0, 0);
        this.strawWestLayer8.addBox(0, 0, 0, 0, 8, 16);
        this.westLayer = new RendererModel[] {this.strawWestLayer1,
                                              this.strawWestLayer2,
                                              this.strawWestLayer3,
                                              this.strawWestLayer4,
                                              this.strawWestLayer5,
                                              this.strawWestLayer6,
                                              this.strawWestLayer7,
                                              this.strawWestLayer8};
        this.strawNorthLayer1 = new RendererModel(this, 0, 23);
        this.strawNorthLayer1.addBox(0, -1, 0, 16, 1, 0);
        this.strawNorthLayer2 = new RendererModel(this, 0, 22);
        this.strawNorthLayer2.addBox(0, -2, 0, 16, 2, 0);
        this.strawNorthLayer3 = new RendererModel(this, 0, 21);
        this.strawNorthLayer3.addBox(0, -3, 0, 16, 3, 0);
        this.strawNorthLayer4 = new RendererModel(this, 0, 20);
        this.strawNorthLayer4.addBox(0, -4, 0, 16, 4, 0);
        this.strawNorthLayer5 = new RendererModel(this, 0, 19);
        this.strawNorthLayer5.addBox(0, -5, 0, 16, 5, 0);
        this.strawNorthLayer6 = new RendererModel(this, 0, 18);
        this.strawNorthLayer6.addBox(0, -6, 0, 16, 6, 0);
        this.strawNorthLayer7 = new RendererModel(this, 0, 17);
        this.strawNorthLayer7.addBox(0, -7, 0, 16, 7, 0);
        this.strawNorthLayer8 = new RendererModel(this, 0, 16);
        this.strawNorthLayer8.addBox(0, -8, 0, 16, 8, 0);
        this.northLayer = new RendererModel[] {this.strawNorthLayer1,
                                               this.strawNorthLayer2,
                                               this.strawNorthLayer3,
                                               this.strawNorthLayer4,
                                               this.strawNorthLayer5,
                                               this.strawNorthLayer6,
                                               this.strawNorthLayer7,
                                               this.strawNorthLayer8};
        for (RendererModel model : this.northLayer) {
            setRotationAngle(model, MathHelper.degToRad(180), 0, 0);
        }
        this.strawSouthLayer1 = new RendererModel(this, 16, 23);
        this.strawSouthLayer1.addBox(0, -1, -16, 16, 1, 0);
        this.strawSouthLayer2 = new RendererModel(this, 16, 22);
        this.strawSouthLayer2.addBox(0, -2, -16, 16, 2, 0);
        this.strawSouthLayer3 = new RendererModel(this, 16, 21);
        this.strawSouthLayer3.addBox(0, -3, -16, 16, 3, 0);
        this.strawSouthLayer4 = new RendererModel(this, 16, 20);
        this.strawSouthLayer4.addBox(0, -4, -16, 16, 4, 0);
        this.strawSouthLayer5 = new RendererModel(this, 16, 19);
        this.strawSouthLayer5.addBox(0, -5, -16, 16, 5, 0);
        this.strawSouthLayer6 = new RendererModel(this, 16, 18);
        this.strawSouthLayer6.addBox(0, -6, -16, 16, 6, 0);
        this.strawSouthLayer7 = new RendererModel(this, 16, 17);
        this.strawSouthLayer7.addBox(0, -7, -16, 16, 7, 0);
        this.strawSouthLayer8 = new RendererModel(this, 16, 16);
        this.strawSouthLayer8.addBox(0, -8, -16, 16, 8, 0);
        this.southLayer = new RendererModel[] {this.strawSouthLayer1,
                                               this.strawSouthLayer2,
                                               this.strawSouthLayer3,
                                               this.strawSouthLayer4,
                                               this.strawSouthLayer5,
                                               this.strawSouthLayer6,
                                               this.strawSouthLayer7,
                                               this.strawSouthLayer8};
        for (RendererModel model : this.southLayer) {
            setRotationAngle(model, MathHelper.degToRad(180), 0, 0);
        }
        this.strawEastLayer1 = new RendererModel(this, -16, 0);
        this.strawEastLayer1.addBox(16, 0, 0, 0, 1, 16);
        this.strawEastLayer2 = new RendererModel(this, -16, 0);
        this.strawEastLayer2.addBox(16, 0, 0, 0, 2, 16);
        this.strawEastLayer3 = new RendererModel(this, -16, 0);
        this.strawEastLayer3.addBox(16, 0, 0, 0, 3, 16);
        this.strawEastLayer4 = new RendererModel(this, -16, 0);
        this.strawEastLayer4.addBox(16, 0, 0, 0, 4, 16);
        this.strawEastLayer5 = new RendererModel(this, -16, 0);
        this.strawEastLayer5.addBox(16, 0, 0, 0, 5, 16);
        this.strawEastLayer6 = new RendererModel(this, -16, 0);
        this.strawEastLayer6.addBox(16, 0, 0, 0, 6, 16);
        this.strawEastLayer7 = new RendererModel(this, -16, 0);
        this.strawEastLayer7.addBox(16, 0, 0, 0, 7, 16);
        this.strawEastLayer8 = new RendererModel(this, -16, 0);
        this.strawEastLayer8.addBox(16, 0, 0, 0, 8, 16);
        this.eastLayer = new RendererModel[] {this.strawEastLayer1,
                                              this.strawEastLayer2,
                                              this.strawEastLayer3,
                                              this.strawEastLayer4,
                                              this.strawEastLayer5,
                                              this.strawEastLayer6,
                                              this.strawEastLayer7,
                                              this.strawEastLayer8};
    }

    public void render(int layers) {
        if (layers == 0) {
            return;
        }
        if (layers <= 8) {
            this.strawTopLayer.offsetY = layers / 16f;
            this.strawTopLayer.render(1 / 16f);
            this.westLayer[layers - 1].render(1 / 16f);
            this.northLayer[layers - 1].render(1 / 16f);
            this.southLayer[layers - 1].render(1 / 16f);
            this.eastLayer[layers - 1].render(1 / 16f);
            return;
        }
        this.strawTopLayer.offsetY = 0.5f;
        this.strawTopLayer.render(1 / 16f);
        this.strawWestLayer8.render(1 / 16f);
        this.strawNorthLayer8.render(1 / 16f);
        this.strawSouthLayer8.render(1 / 16f);
        this.strawEastLayer8.render(1 / 16f);
    }

    public static void setRotationAngle(RendererModel modelRenderer, float x, float y, float z) {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}
