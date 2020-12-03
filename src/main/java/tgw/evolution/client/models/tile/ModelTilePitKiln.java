package tgw.evolution.client.models.tile;

import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.client.renderer.model.Model;
import tgw.evolution.util.MathHelper;

public class ModelTilePitKiln extends Model {

    private final RendererModel strawTopLayer;
    private final RendererModel strawWestLayer8;
    private final RendererModel[] westLayer;
    private final RendererModel strawNorthLayer8;
    private final RendererModel[] northLayer;
    private final RendererModel strawSouthLayer8;
    private final RendererModel[] southLayer;
    private final RendererModel strawEastLayer8;
    private final RendererModel[] eastLayer;

    public ModelTilePitKiln() {
        this.textureHeight = 24;
        this.textureWidth = 32;
        this.strawTopLayer = new RendererModel(this, -16, 0);
        this.strawTopLayer.addBox(0, 0, 0, 16, 0, 16);
        RendererModel strawWestLayer1 = new RendererModel(this, 0, 0);
        strawWestLayer1.addBox(0, 0, 0, 0, 1, 16);
        RendererModel strawWestLayer2 = new RendererModel(this, 0, 0);
        strawWestLayer2.addBox(0, 0, 0, 0, 2, 16);
        RendererModel strawWestLayer3 = new RendererModel(this, 0, 0);
        strawWestLayer3.addBox(0, 0, 0, 0, 3, 16);
        RendererModel strawWestLayer4 = new RendererModel(this, 0, 0);
        strawWestLayer4.addBox(0, 0, 0, 0, 4, 16);
        RendererModel strawWestLayer5 = new RendererModel(this, 0, 0);
        strawWestLayer5.addBox(0, 0, 0, 0, 5, 16);
        RendererModel strawWestLayer6 = new RendererModel(this, 0, 0);
        strawWestLayer6.addBox(0, 0, 0, 0, 6, 16);
        RendererModel strawWestLayer7 = new RendererModel(this, 0, 0);
        strawWestLayer7.addBox(0, 0, 0, 0, 7, 16);
        this.strawWestLayer8 = new RendererModel(this, 0, 0);
        this.strawWestLayer8.addBox(0, 0, 0, 0, 8, 16);
        this.westLayer = new RendererModel[]{strawWestLayer1,
                                             strawWestLayer2,
                                             strawWestLayer3,
                                             strawWestLayer4,
                                             strawWestLayer5,
                                             strawWestLayer6,
                                             strawWestLayer7,
                                             this.strawWestLayer8};
        RendererModel strawNorthLayer1 = new RendererModel(this, 0, 23);
        strawNorthLayer1.addBox(0, -1, 0, 16, 1, 0);
        RendererModel strawNorthLayer2 = new RendererModel(this, 0, 22);
        strawNorthLayer2.addBox(0, -2, 0, 16, 2, 0);
        RendererModel strawNorthLayer3 = new RendererModel(this, 0, 21);
        strawNorthLayer3.addBox(0, -3, 0, 16, 3, 0);
        RendererModel strawNorthLayer4 = new RendererModel(this, 0, 20);
        strawNorthLayer4.addBox(0, -4, 0, 16, 4, 0);
        RendererModel strawNorthLayer5 = new RendererModel(this, 0, 19);
        strawNorthLayer5.addBox(0, -5, 0, 16, 5, 0);
        RendererModel strawNorthLayer6 = new RendererModel(this, 0, 18);
        strawNorthLayer6.addBox(0, -6, 0, 16, 6, 0);
        RendererModel strawNorthLayer7 = new RendererModel(this, 0, 17);
        strawNorthLayer7.addBox(0, -7, 0, 16, 7, 0);
        this.strawNorthLayer8 = new RendererModel(this, 0, 16);
        this.strawNorthLayer8.addBox(0, -8, 0, 16, 8, 0);
        this.northLayer = new RendererModel[]{strawNorthLayer1,
                                              strawNorthLayer2,
                                              strawNorthLayer3,
                                              strawNorthLayer4,
                                              strawNorthLayer5,
                                              strawNorthLayer6,
                                              strawNorthLayer7,
                                              this.strawNorthLayer8};
        for (RendererModel model : this.northLayer) {
            MathHelper.setRotationAngle(model, MathHelper.PI, 0, 0);
        }
        RendererModel strawSouthLayer1 = new RendererModel(this, 16, 23);
        strawSouthLayer1.addBox(0, -1, -16, 16, 1, 0);
        RendererModel strawSouthLayer2 = new RendererModel(this, 16, 22);
        strawSouthLayer2.addBox(0, -2, -16, 16, 2, 0);
        RendererModel strawSouthLayer3 = new RendererModel(this, 16, 21);
        strawSouthLayer3.addBox(0, -3, -16, 16, 3, 0);
        RendererModel strawSouthLayer4 = new RendererModel(this, 16, 20);
        strawSouthLayer4.addBox(0, -4, -16, 16, 4, 0);
        RendererModel strawSouthLayer5 = new RendererModel(this, 16, 19);
        strawSouthLayer5.addBox(0, -5, -16, 16, 5, 0);
        RendererModel strawSouthLayer6 = new RendererModel(this, 16, 18);
        strawSouthLayer6.addBox(0, -6, -16, 16, 6, 0);
        RendererModel strawSouthLayer7 = new RendererModel(this, 16, 17);
        strawSouthLayer7.addBox(0, -7, -16, 16, 7, 0);
        this.strawSouthLayer8 = new RendererModel(this, 16, 16);
        this.strawSouthLayer8.addBox(0, -8, -16, 16, 8, 0);
        this.southLayer = new RendererModel[]{strawSouthLayer1,
                                              strawSouthLayer2,
                                              strawSouthLayer3,
                                              strawSouthLayer4,
                                              strawSouthLayer5,
                                              strawSouthLayer6,
                                              strawSouthLayer7,
                                              this.strawSouthLayer8};
        for (RendererModel model : this.southLayer) {
            MathHelper.setRotationAngle(model, MathHelper.PI, 0, 0);
        }
        RendererModel strawEastLayer1 = new RendererModel(this, -16, 0);
        strawEastLayer1.addBox(16, 0, 0, 0, 1, 16);
        RendererModel strawEastLayer2 = new RendererModel(this, -16, 0);
        strawEastLayer2.addBox(16, 0, 0, 0, 2, 16);
        RendererModel strawEastLayer3 = new RendererModel(this, -16, 0);
        strawEastLayer3.addBox(16, 0, 0, 0, 3, 16);
        RendererModel strawEastLayer4 = new RendererModel(this, -16, 0);
        strawEastLayer4.addBox(16, 0, 0, 0, 4, 16);
        RendererModel strawEastLayer5 = new RendererModel(this, -16, 0);
        strawEastLayer5.addBox(16, 0, 0, 0, 5, 16);
        RendererModel strawEastLayer6 = new RendererModel(this, -16, 0);
        strawEastLayer6.addBox(16, 0, 0, 0, 6, 16);
        RendererModel strawEastLayer7 = new RendererModel(this, -16, 0);
        strawEastLayer7.addBox(16, 0, 0, 0, 7, 16);
        this.strawEastLayer8 = new RendererModel(this, -16, 0);
        this.strawEastLayer8.addBox(16, 0, 0, 0, 8, 16);
        this.eastLayer = new RendererModel[]{strawEastLayer1,
                                             strawEastLayer2,
                                             strawEastLayer3,
                                             strawEastLayer4,
                                             strawEastLayer5,
                                             strawEastLayer6,
                                             strawEastLayer7,
                                             this.strawEastLayer8};
    }

    public void render(int layers) {
        if (layers == 0) {
            return;
        }
        if (layers <= 8) {
            this.strawTopLayer.offsetY = layers / 16.0f;
            this.strawTopLayer.render(1 / 16.0f);
            this.westLayer[layers - 1].render(1 / 16.0f);
            this.northLayer[layers - 1].render(1 / 16.0f);
            this.southLayer[layers - 1].render(1 / 16.0f);
            this.eastLayer[layers - 1].render(1 / 16.0f);
            return;
        }
        this.strawTopLayer.offsetY = 0.5f;
        this.strawTopLayer.render(1 / 16.0f);
        this.strawWestLayer8.render(1 / 16.0f);
        this.strawNorthLayer8.render(1 / 16.0f);
        this.strawSouthLayer8.render(1 / 16.0f);
        this.strawEastLayer8.render(1 / 16.0f);
    }
}
