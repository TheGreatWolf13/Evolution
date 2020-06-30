package tgw.evolution.client.models.tile;

import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.client.renderer.model.Model;
import tgw.evolution.util.MathHelper;

public class ModelTileKnapping extends Model {

    private final RendererModel AA;
    private final RendererModel AB;
    private final RendererModel AC;
    private final RendererModel AD;
    private final RendererModel AE;
    private final RendererModel BA;
    private final RendererModel BB;
    private final RendererModel BC;
    private final RendererModel BD;
    private final RendererModel BE;
    private final RendererModel CA;
    private final RendererModel CB;
    private final RendererModel CC;
    private final RendererModel CD;
    private final RendererModel CE;
    private final RendererModel DA;
    private final RendererModel DB;
    private final RendererModel DC;
    private final RendererModel DD;
    private final RendererModel DE;
    private final RendererModel EA;
    private final RendererModel EB;
    private final RendererModel EC;
    private final RendererModel ED;
    private final RendererModel EE;
    public final RendererModel[][] cubes;

    public ModelTileKnapping() {
        super.textureWidth = 64;
        super.textureHeight = 32;
        this.AA = new RendererModel(this, 0, 0);
        this.AA.addBox(0.5F, -1.0F, -1.5F, 3, 1, 3);
        this.AB = new RendererModel(this, 12, 0);
        this.AB.addBox(3.5F, -1.0F, -1.5F, 3, 1, 3);
        this.AC = new RendererModel(this, 24, 0);
        this.AC.addBox(6.5F, -1.0F, -1.5F, 3, 1, 3);
        this.AD = new RendererModel(this, 36, 0);
        this.AD.addBox(9.5F, -1.0F, -1.5F, 3, 1, 3);
        this.AE = new RendererModel(this, 48, 0);
        this.AE.addBox(12.5F, -1.0F, -1.5F, 3, 1, 3);
        this.BA = new RendererModel(this, 0, 4);
        this.BA.addBox(0.5F, -1.0F, -4.5F, 3, 1, 3);
        this.BB = new RendererModel(this, 12, 4);
        this.BB.addBox(3.5F, -1.0F, -4.5F, 3, 1, 3);
        this.BC = new RendererModel(this, 24, 4);
        this.BC.addBox(6.5F, -1.0F, -4.5F, 3, 1, 3);
        this.BD = new RendererModel(this, 36, 4);
        this.BD.addBox(9.5F, -1.0F, -4.5F, 3, 1, 3);
        this.BE = new RendererModel(this, 48, 4);
        this.BE.addBox(12.5F, -1.0F, -4.5F, 3, 1, 3);
        this.CA = new RendererModel(this, 0, 8);
        this.CA.addBox(0.5F, -1.0F, -7.5F, 3, 1, 3);
        this.CB = new RendererModel(this, 12, 8);
        this.CB.addBox(3.5F, -1.0F, -7.5F, 3, 1, 3);
        this.CC = new RendererModel(this, 24, 8);
        this.CC.addBox(6.5F, -1.0F, -7.5F, 3, 1, 3);
        this.CD = new RendererModel(this, 36, 8);
        this.CD.addBox(9.5F, -1.0F, -7.5F, 3, 1, 3);
        this.CE = new RendererModel(this, 48, 8);
        this.CE.addBox(12.5F, -1.0F, -7.5F, 3, 1, 3);
        this.DA = new RendererModel(this, 0, 12);
        this.DA.addBox(0.5F, -1.0F, -10.5F, 3, 1, 3);
        this.DB = new RendererModel(this, 12, 12);
        this.DB.addBox(3.5F, -1.0F, -10.5F, 3, 1, 3);
        this.DC = new RendererModel(this, 24, 12);
        this.DC.addBox(6.5F, -1.0F, -10.5F, 3, 1, 3);
        this.DD = new RendererModel(this, 36, 12);
        this.DD.addBox(9.5F, -1.0F, -10.5F, 3, 1, 3);
        this.DE = new RendererModel(this, 48, 12);
        this.DE.addBox(12.5F, -1.0F, -10.5F, 3, 1, 3);
        this.EA = new RendererModel(this, 0, 16);
        this.EA.addBox(0.5F, -1.0F, -13.5F, 3, 1, 3);
        this.EB = new RendererModel(this, 12, 16);
        this.EB.addBox(3.5F, -1.0F, -13.5F, 3, 1, 3);
        this.EC = new RendererModel(this, 24, 16);
        this.EC.addBox(6.5F, -1.0F, -13.5F, 3, 1, 3);
        this.ED = new RendererModel(this, 36, 16);
        this.ED.addBox(9.5F, -1.0F, -13.5F, 3, 1, 3);
        this.EE = new RendererModel(this, 48, 16);
        this.EE.addBox(12.5F, -1.0F, -13.5F, 3, 1, 3);
        this.cubes = new RendererModel[][] {{this.AA, this.BA, this.CA, this.DA, this.EA}, {this.AB, this.BB, this.CB, this.DB, this.EB},
                                            {this.AC, this.BC, this.CC, this.DC, this.EC}, {this.AD, this.BD, this.CD, this.DD, this.ED},
                                            {this.AE, this.BE, this.CE, this.DE, this.EE}};
        for (RendererModel[] cube : this.cubes) {
            for (RendererModel rendererModel : cube) {
                setRotationPivot(rendererModel, 0, 0, 2);
                setRotationAngle(rendererModel, MathHelper.degToRad(180), 0, 0);
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

    public static void setRotationAngle(RendererModel modelRenderer, float x, float y, float z) {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }

    public static void setRotationPivot(RendererModel modelRenderer, float x, float y, float z) {
        modelRenderer.rotationPointX = x;
        modelRenderer.rotationPointY = y;
        modelRenderer.rotationPointZ = z;
    }
}
