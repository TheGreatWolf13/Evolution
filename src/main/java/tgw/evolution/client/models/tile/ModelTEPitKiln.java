package tgw.evolution.client.models.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.RenderType;

public class ModelTEPitKiln extends Model {

//    private final ModelRenderer[] eastLayer;
//    private final ModelRenderer[] northLayer;
//    private final ModelRenderer[] southLayer;
//    private final ModelRenderer strawEastLayer8;
//    private final ModelRenderer strawNorthLayer8;
//    private final ModelRenderer strawSouthLayer8;
//    private final ModelRenderer strawTopLayer;
//    private final ModelRenderer strawWestLayer8;
//    private final ModelRenderer[] westLayer;

    public ModelTEPitKiln() {
        super(RenderType::entitySolid);
//        this.texHeight = 24;
//        this.texWidth = 32;
//        this.strawTopLayer = new ModelRenderer(this, -16, 0);
//        this.strawTopLayer.addBox(0, 0, 0, 16, 0, 16);
//        ModelRenderer strawWestLayer1 = new ModelRenderer(this, 0, 0);
//        strawWestLayer1.addBox(0, 0, 0, 0, 1, 16);
//        ModelRenderer strawWestLayer2 = new ModelRenderer(this, 0, 0);
//        strawWestLayer2.addBox(0, 0, 0, 0, 2, 16);
//        ModelRenderer strawWestLayer3 = new ModelRenderer(this, 0, 0);
//        strawWestLayer3.addBox(0, 0, 0, 0, 3, 16);
//        ModelRenderer strawWestLayer4 = new ModelRenderer(this, 0, 0);
//        strawWestLayer4.addBox(0, 0, 0, 0, 4, 16);
//        ModelRenderer strawWestLayer5 = new ModelRenderer(this, 0, 0);
//        strawWestLayer5.addBox(0, 0, 0, 0, 5, 16);
//        ModelRenderer strawWestLayer6 = new ModelRenderer(this, 0, 0);
//        strawWestLayer6.addBox(0, 0, 0, 0, 6, 16);
//        ModelRenderer strawWestLayer7 = new ModelRenderer(this, 0, 0);
//        strawWestLayer7.addBox(0, 0, 0, 0, 7, 16);
//        this.strawWestLayer8 = new ModelRenderer(this, 0, 0);
//        this.strawWestLayer8.addBox(0, 0, 0, 0, 8, 16);
//        this.westLayer = new ModelRenderer[]{strawWestLayer1,
//                                             strawWestLayer2,
//                                             strawWestLayer3,
//                                             strawWestLayer4,
//                                             strawWestLayer5,
//                                             strawWestLayer6,
//                                             strawWestLayer7,
//                                             this.strawWestLayer8};
//        ModelRenderer strawNorthLayer1 = new ModelRenderer(this, 0, 23);
//        strawNorthLayer1.addBox(0, -1, 0, 16, 1, 0);
//        ModelRenderer strawNorthLayer2 = new ModelRenderer(this, 0, 22);
//        strawNorthLayer2.addBox(0, -2, 0, 16, 2, 0);
//        ModelRenderer strawNorthLayer3 = new ModelRenderer(this, 0, 21);
//        strawNorthLayer3.addBox(0, -3, 0, 16, 3, 0);
//        ModelRenderer strawNorthLayer4 = new ModelRenderer(this, 0, 20);
//        strawNorthLayer4.addBox(0, -4, 0, 16, 4, 0);
//        ModelRenderer strawNorthLayer5 = new ModelRenderer(this, 0, 19);
//        strawNorthLayer5.addBox(0, -5, 0, 16, 5, 0);
//        ModelRenderer strawNorthLayer6 = new ModelRenderer(this, 0, 18);
//        strawNorthLayer6.addBox(0, -6, 0, 16, 6, 0);
//        ModelRenderer strawNorthLayer7 = new ModelRenderer(this, 0, 17);
//        strawNorthLayer7.addBox(0, -7, 0, 16, 7, 0);
//        this.strawNorthLayer8 = new ModelRenderer(this, 0, 16);
//        this.strawNorthLayer8.addBox(0, -8, 0, 16, 8, 0);
//        this.northLayer = new ModelRenderer[]{strawNorthLayer1,
//                                              strawNorthLayer2,
//                                              strawNorthLayer3,
//                                              strawNorthLayer4,
//                                              strawNorthLayer5,
//                                              strawNorthLayer6,
//                                              strawNorthLayer7,
//                                              this.strawNorthLayer8};
//        for (ModelRenderer model : this.northLayer) {
//            MathHelper.setRotationAngle(model, MathHelper.PI, 0, 0);
//        }
//        ModelRenderer strawSouthLayer1 = new ModelRenderer(this, 16, 23);
//        strawSouthLayer1.addBox(0, -1, -16, 16, 1, 0);
//        ModelRenderer strawSouthLayer2 = new ModelRenderer(this, 16, 22);
//        strawSouthLayer2.addBox(0, -2, -16, 16, 2, 0);
//        ModelRenderer strawSouthLayer3 = new ModelRenderer(this, 16, 21);
//        strawSouthLayer3.addBox(0, -3, -16, 16, 3, 0);
//        ModelRenderer strawSouthLayer4 = new ModelRenderer(this, 16, 20);
//        strawSouthLayer4.addBox(0, -4, -16, 16, 4, 0);
//        ModelRenderer strawSouthLayer5 = new ModelRenderer(this, 16, 19);
//        strawSouthLayer5.addBox(0, -5, -16, 16, 5, 0);
//        ModelRenderer strawSouthLayer6 = new ModelRenderer(this, 16, 18);
//        strawSouthLayer6.addBox(0, -6, -16, 16, 6, 0);
//        ModelRenderer strawSouthLayer7 = new ModelRenderer(this, 16, 17);
//        strawSouthLayer7.addBox(0, -7, -16, 16, 7, 0);
//        this.strawSouthLayer8 = new ModelRenderer(this, 16, 16);
//        this.strawSouthLayer8.addBox(0, -8, -16, 16, 8, 0);
//        this.southLayer = new ModelRenderer[]{strawSouthLayer1,
//                                              strawSouthLayer2,
//                                              strawSouthLayer3,
//                                              strawSouthLayer4,
//                                              strawSouthLayer5,
//                                              strawSouthLayer6,
//                                              strawSouthLayer7,
//                                              this.strawSouthLayer8};
//        for (ModelRenderer model : this.southLayer) {
//            MathHelper.setRotationAngle(model, MathHelper.PI, 0, 0);
//        }
//        ModelRenderer strawEastLayer1 = new ModelRenderer(this, -16, 0);
//        strawEastLayer1.addBox(16, 0, 0, 0, 1, 16);
//        ModelRenderer strawEastLayer2 = new ModelRenderer(this, -16, 0);
//        strawEastLayer2.addBox(16, 0, 0, 0, 2, 16);
//        ModelRenderer strawEastLayer3 = new ModelRenderer(this, -16, 0);
//        strawEastLayer3.addBox(16, 0, 0, 0, 3, 16);
//        ModelRenderer strawEastLayer4 = new ModelRenderer(this, -16, 0);
//        strawEastLayer4.addBox(16, 0, 0, 0, 4, 16);
//        ModelRenderer strawEastLayer5 = new ModelRenderer(this, -16, 0);
//        strawEastLayer5.addBox(16, 0, 0, 0, 5, 16);
//        ModelRenderer strawEastLayer6 = new ModelRenderer(this, -16, 0);
//        strawEastLayer6.addBox(16, 0, 0, 0, 6, 16);
//        ModelRenderer strawEastLayer7 = new ModelRenderer(this, -16, 0);
//        strawEastLayer7.addBox(16, 0, 0, 0, 7, 16);
//        this.strawEastLayer8 = new ModelRenderer(this, -16, 0);
//        this.strawEastLayer8.addBox(16, 0, 0, 0, 8, 16);
//        this.eastLayer = new ModelRenderer[]{strawEastLayer1,
//                                             strawEastLayer2,
//                                             strawEastLayer3,
//                                             strawEastLayer4,
//                                             strawEastLayer5,
//                                             strawEastLayer6,
//                                             strawEastLayer7,
//                                             this.strawEastLayer8};
    }

    @Override
    public void renderToBuffer(PoseStack matrices,
                               VertexConsumer buffer,
                               int packedLight,
                               int packedOverlay,
                               float red,
                               float green,
                               float blue,
                               float alpha) {
//        this.strawTopLayer.render(matrices, buffer, packedLight, packedOverlay, red, green, blue, alpha);
//        for (ModelRenderer modelRenderer : this.westLayer) {
//            modelRenderer.render(matrices, buffer, packedLight, packedOverlay, red, green, blue, alpha);
//        }
//        for (ModelRenderer modelRenderer : this.northLayer) {
//            modelRenderer.render(matrices, buffer, packedLight, packedOverlay, red, green, blue, alpha);
//        }
//        for (ModelRenderer modelRenderer : this.southLayer) {
//            modelRenderer.render(matrices, buffer, packedLight, packedOverlay, red, green, blue, alpha);
//        }
//        for (ModelRenderer modelRenderer : this.eastLayer) {
//            modelRenderer.render(matrices, buffer, packedLight, packedOverlay, red, green, blue, alpha);
//        }
    }

    private void reset() {
//        this.strawTopLayer.visible = false;
//        for (ModelRenderer modelRenderer : this.westLayer) {
//            modelRenderer.visible = false;
//        }
//        for (ModelRenderer modelRenderer : this.northLayer) {
//            modelRenderer.visible = false;
//        }
//        for (ModelRenderer modelRenderer : this.southLayer) {
//            modelRenderer.visible = false;
//        }
//        for (ModelRenderer modelRenderer : this.eastLayer) {
//            modelRenderer.visible = false;
//        }
    }

    public void setup(int layers) {
        this.reset();
        if (layers == 0) {
            return;
        }
        if (layers <= 8) {
            //TODO
//            this.strawTopLayer.offsetY = layers / 16.0f;
//            this.strawTopLayer.visible = true;
//            this.westLayer[layers - 1].visible = true;
//            this.northLayer[layers - 1].visible = true;
//            this.southLayer[layers - 1].visible = true;
//            this.eastLayer[layers - 1].visible = true;
            return;
        }
        //TODO
//        this.strawTopLayer.offsetY = 0.5f;
//        this.strawTopLayer.visible = true;
//        this.strawWestLayer8.visible = true;
//        this.strawNorthLayer8.visible = true;
//        this.strawSouthLayer8.visible = true;
//        this.strawEastLayer8.visible = true;
    }
}
