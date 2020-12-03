package tgw.evolution.client.models.tile;

import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.client.renderer.model.Model;
import tgw.evolution.util.MathHelper;

public class ModelTileKnapping extends Model {

    public final RendererModel[][] cubes;

    public ModelTileKnapping() {
        super.textureWidth = 64;
        super.textureHeight = 32;
        RendererModel AA = new RendererModel(this, 0, 0);
        AA.addBox(0.5F, -1.0F, -1.5F, 3, 1, 3);
        RendererModel AB = new RendererModel(this, 12, 0);
        AB.addBox(3.5F, -1.0F, -1.5F, 3, 1, 3);
        RendererModel AC = new RendererModel(this, 24, 0);
        AC.addBox(6.5F, -1.0F, -1.5F, 3, 1, 3);
        RendererModel AD = new RendererModel(this, 36, 0);
        AD.addBox(9.5F, -1.0F, -1.5F, 3, 1, 3);
        RendererModel AE = new RendererModel(this, 48, 0);
        AE.addBox(12.5F, -1.0F, -1.5F, 3, 1, 3);
        RendererModel BA = new RendererModel(this, 0, 4);
        BA.addBox(0.5F, -1.0F, -4.5F, 3, 1, 3);
        RendererModel BB = new RendererModel(this, 12, 4);
        BB.addBox(3.5F, -1.0F, -4.5F, 3, 1, 3);
        RendererModel BC = new RendererModel(this, 24, 4);
        BC.addBox(6.5F, -1.0F, -4.5F, 3, 1, 3);
        RendererModel BD = new RendererModel(this, 36, 4);
        BD.addBox(9.5F, -1.0F, -4.5F, 3, 1, 3);
        RendererModel BE = new RendererModel(this, 48, 4);
        BE.addBox(12.5F, -1.0F, -4.5F, 3, 1, 3);
        RendererModel CA = new RendererModel(this, 0, 8);
        CA.addBox(0.5F, -1.0F, -7.5F, 3, 1, 3);
        RendererModel CB = new RendererModel(this, 12, 8);
        CB.addBox(3.5F, -1.0F, -7.5F, 3, 1, 3);
        RendererModel CC = new RendererModel(this, 24, 8);
        CC.addBox(6.5F, -1.0F, -7.5F, 3, 1, 3);
        RendererModel CD = new RendererModel(this, 36, 8);
        CD.addBox(9.5F, -1.0F, -7.5F, 3, 1, 3);
        RendererModel CE = new RendererModel(this, 48, 8);
        CE.addBox(12.5F, -1.0F, -7.5F, 3, 1, 3);
        RendererModel DA = new RendererModel(this, 0, 12);
        DA.addBox(0.5F, -1.0F, -10.5F, 3, 1, 3);
        RendererModel DB = new RendererModel(this, 12, 12);
        DB.addBox(3.5F, -1.0F, -10.5F, 3, 1, 3);
        RendererModel DC = new RendererModel(this, 24, 12);
        DC.addBox(6.5F, -1.0F, -10.5F, 3, 1, 3);
        RendererModel DD = new RendererModel(this, 36, 12);
        DD.addBox(9.5F, -1.0F, -10.5F, 3, 1, 3);
        RendererModel DE = new RendererModel(this, 48, 12);
        DE.addBox(12.5F, -1.0F, -10.5F, 3, 1, 3);
        RendererModel EA = new RendererModel(this, 0, 16);
        EA.addBox(0.5F, -1.0F, -13.5F, 3, 1, 3);
        RendererModel EB = new RendererModel(this, 12, 16);
        EB.addBox(3.5F, -1.0F, -13.5F, 3, 1, 3);
        RendererModel EC = new RendererModel(this, 24, 16);
        EC.addBox(6.5F, -1.0F, -13.5F, 3, 1, 3);
        RendererModel ED = new RendererModel(this, 36, 16);
        ED.addBox(9.5F, -1.0F, -13.5F, 3, 1, 3);
        RendererModel EE = new RendererModel(this, 48, 16);
        EE.addBox(12.5F, -1.0F, -13.5F, 3, 1, 3);
        this.cubes = new RendererModel[][]{{AA, BA, CA, DA, EA},
                                           {AB, BB, CB, DB, EB},
                                           {AC, BC, CC, DC, EC},
                                           {AD, BD, CD, DD, ED},
                                           {AE, BE, CE, DE, EE}};
        for (RendererModel[] cube : this.cubes) {
            for (RendererModel rendererModel : cube) {
                MathHelper.setRotationPivot(rendererModel, 0, 0, 2);
                MathHelper.setRotationAngle(rendererModel, MathHelper.PI, 0, 0);
            }
        }
    }

    public void renderAll(boolean[][] matrix) {
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                if (matrix[i][j]) {
                    this.cubes[i][j].render(1.0F / 16.0F);
                }
            }
        }
    }
}
