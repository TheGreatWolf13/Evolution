package tgw.evolution.client.models.tile;

import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.client.renderer.model.Model;
import tgw.evolution.util.MathHelper;

public class ModelTileMolding extends Model {

    public final RendererModel[][] layer1;
    public final RendererModel[][] layer2;
    public final RendererModel[][] layer3;
    public final RendererModel[][] layer4;
    public final RendererModel[][] layer5;
    private final RendererModel base;
    private final RendererModel baseN;
    private final RendererModel baseS;
    private final RendererModel baseW;
    private final RendererModel baseE;
    private final RendererModel[][][] layers;

    public ModelTileMolding() {
        super.textureWidth = 128;
        super.textureHeight = 40;
        this.base = new RendererModel(this, 0, 0);
        this.base.addBox(0.0F, 0.0F, 0.0F, 32, 1, 32);
        this.baseN = new RendererModel(this, 0, 33);
        this.baseN.addBox(0.0F, 1.0F, 0.0F, 32, 5, 1);
        this.baseS = new RendererModel(this, 0, 33);
        this.baseS.addBox(0.0F, 1.0F, 31.0F, 32, 5, 1);
        this.baseW = new RendererModel(this, 66, 33);
        this.baseW.addBox(-31.0F, 1.0F, 0.0F, 30, 5, 1);
        MathHelper.setRotationAngle(this.baseW, MathHelper.PI_OVER_2, MathHelper.PI_OVER_2, MathHelper.PI_OVER_2);
        this.baseE = new RendererModel(this, 66, 33);
        this.baseE.addBox(-31.0F, 1.0F, 31.0F, 30, 5, 1);
        MathHelper.setRotationAngle(this.baseE, MathHelper.PI_OVER_2, MathHelper.PI_OVER_2, MathHelper.PI_OVER_2);
        RendererModel AA1 = new RendererModel(this, 0, 0);
        AA1.addBox(1.0F, 0.0F, 1.0F, 6, 6, 6);
        RendererModel AB1 = new RendererModel(this, 0, 0);
        AB1.addBox(7.0F, 0.0F, 1.0F, 6, 6, 6);
        RendererModel AC1 = new RendererModel(this, 0, 0);
        AC1.addBox(13.0F, 0.0F, 1.0F, 6, 6, 6);
        RendererModel AD1 = new RendererModel(this, 0, 0);
        AD1.addBox(19.0F, 0.0F, 1.0F, 6, 6, 6);
        RendererModel AE1 = new RendererModel(this, 0, 0);
        AE1.addBox(25.0F, 0.0F, 1.0F, 6, 6, 6);
        RendererModel BA1 = new RendererModel(this, 0, 0);
        BA1.addBox(1.0F, 0.0F, 7.0F, 6, 6, 6);
        RendererModel BB1 = new RendererModel(this, 0, 0);
        BB1.addBox(7.0F, 0.0F, 7.0F, 6, 6, 6);
        RendererModel BC1 = new RendererModel(this, 0, 0);
        BC1.addBox(13.0F, 0.0F, 7.0F, 6, 6, 6);
        RendererModel BD1 = new RendererModel(this, 0, 0);
        BD1.addBox(19.0F, 0.0F, 7.0F, 6, 6, 6);
        RendererModel BE1 = new RendererModel(this, 0, 0);
        BE1.addBox(25.0F, 0.0F, 7.0F, 6, 6, 6);
        RendererModel CA1 = new RendererModel(this, 0, 0);
        CA1.addBox(1.0F, 0.0F, 13.0F, 6, 6, 6);
        RendererModel CB1 = new RendererModel(this, 0, 0);
        CB1.addBox(7.0F, 0.0F, 13.0F, 6, 6, 6);
        RendererModel CC1 = new RendererModel(this, 0, 0);
        CC1.addBox(13.0F, 0.0F, 13.0F, 6, 6, 6);
        RendererModel CD1 = new RendererModel(this, 0, 0);
        CD1.addBox(19.0F, 0.0F, 13.0F, 6, 6, 6);
        RendererModel CE1 = new RendererModel(this, 0, 0);
        CE1.addBox(25.0F, 0.0F, 13.0F, 6, 6, 6);
        RendererModel DA1 = new RendererModel(this, 0, 0);
        DA1.addBox(1.0F, 0.0F, 19.0F, 6, 6, 6);
        RendererModel DB1 = new RendererModel(this, 0, 0);
        DB1.addBox(7.0F, 0.0F, 19.0F, 6, 6, 6);
        RendererModel DC1 = new RendererModel(this, 0, 0);
        DC1.addBox(13.0F, 0.0F, 19.0F, 6, 6, 6);
        RendererModel DD1 = new RendererModel(this, 0, 0);
        DD1.addBox(19.0F, 0.0F, 19.0F, 6, 6, 6);
        RendererModel DE1 = new RendererModel(this, 0, 0);
        DE1.addBox(25.0F, 0.0F, 19.0F, 6, 6, 6);
        RendererModel EA1 = new RendererModel(this, 0, 0);
        EA1.addBox(1.0F, 0.0F, 25.0F, 6, 6, 6);
        RendererModel EB1 = new RendererModel(this, 0, 0);
        EB1.addBox(7.0F, 0.0F, 25.0F, 6, 6, 6);
        RendererModel EC1 = new RendererModel(this, 0, 0);
        EC1.addBox(13.0F, 0.0F, 25.0F, 6, 6, 6);
        RendererModel ED1 = new RendererModel(this, 0, 0);
        ED1.addBox(19.0F, 0.0F, 25.0F, 6, 6, 6);
        RendererModel EE1 = new RendererModel(this, 0, 0);
        EE1.addBox(25.0F, 0.0F, 25.0F, 6, 6, 6);
        RendererModel AA2 = new RendererModel(this, 0, 0);
        AA2.addBox(1.0F, 6.0F, 1.0F, 6, 6, 6);
        RendererModel AB2 = new RendererModel(this, 0, 0);
        AB2.addBox(7.0F, 6.0F, 1.0F, 6, 6, 6);
        RendererModel AC2 = new RendererModel(this, 0, 0);
        AC2.addBox(13.0F, 6.0F, 1.0F, 6, 6, 6);
        RendererModel AD2 = new RendererModel(this, 0, 0);
        AD2.addBox(19.0F, 6.0F, 1.0F, 6, 6, 6);
        RendererModel AE2 = new RendererModel(this, 0, 0);
        AE2.addBox(25.0F, 6.0F, 1.0F, 6, 6, 6);
        RendererModel BA2 = new RendererModel(this, 0, 0);
        BA2.addBox(1.0F, 6.0F, 7.0F, 6, 6, 6);
        RendererModel BB2 = new RendererModel(this, 0, 0);
        BB2.addBox(7.0F, 6.0F, 7.0F, 6, 6, 6);
        RendererModel BC2 = new RendererModel(this, 0, 0);
        BC2.addBox(13.0F, 6.0F, 7.0F, 6, 6, 6);
        RendererModel BD2 = new RendererModel(this, 0, 0);
        BD2.addBox(19.0F, 6.0F, 7.0F, 6, 6, 6);
        RendererModel BE2 = new RendererModel(this, 0, 0);
        BE2.addBox(25.0F, 6.0F, 7.0F, 6, 6, 6);
        RendererModel CA2 = new RendererModel(this, 0, 0);
        CA2.addBox(1.0F, 6.0F, 13.0F, 6, 6, 6);
        RendererModel CB2 = new RendererModel(this, 0, 0);
        CB2.addBox(7.0F, 6.0F, 13.0F, 6, 6, 6);
        RendererModel CC2 = new RendererModel(this, 0, 0);
        CC2.addBox(13.0F, 6.0F, 13.0F, 6, 6, 6);
        RendererModel CD2 = new RendererModel(this, 0, 0);
        CD2.addBox(19.0F, 6.0F, 13.0F, 6, 6, 6);
        RendererModel CE2 = new RendererModel(this, 0, 0);
        CE2.addBox(25.0F, 6.0F, 13.0F, 6, 6, 6);
        RendererModel DA2 = new RendererModel(this, 0, 0);
        DA2.addBox(1.0F, 6.0F, 19.0F, 6, 6, 6);
        RendererModel DB2 = new RendererModel(this, 0, 0);
        DB2.addBox(7.0F, 6.0F, 19.0F, 6, 6, 6);
        RendererModel DC2 = new RendererModel(this, 0, 0);
        DC2.addBox(13.0F, 6.0F, 19.0F, 6, 6, 6);
        RendererModel DD2 = new RendererModel(this, 0, 0);
        DD2.addBox(19.0F, 6.0F, 19.0F, 6, 6, 6);
        RendererModel DE2 = new RendererModel(this, 0, 0);
        DE2.addBox(25.0F, 6.0F, 19.0F, 6, 6, 6);
        RendererModel EA2 = new RendererModel(this, 0, 0);
        EA2.addBox(1.0F, 6.0F, 25.0F, 6, 6, 6);
        RendererModel EB2 = new RendererModel(this, 0, 0);
        EB2.addBox(7.0F, 6.0F, 25.0F, 6, 6, 6);
        RendererModel EC2 = new RendererModel(this, 0, 0);
        EC2.addBox(13.0F, 6.0F, 25.0F, 6, 6, 6);
        RendererModel ED2 = new RendererModel(this, 0, 0);
        ED2.addBox(19.0F, 6.0F, 25.0F, 6, 6, 6);
        RendererModel EE2 = new RendererModel(this, 0, 0);
        EE2.addBox(25.0F, 6.0F, 25.0F, 6, 6, 6);
        RendererModel AA3 = new RendererModel(this, 0, 0);
        AA3.addBox(1.0F, 12.0F, 1.0F, 6, 6, 6);
        RendererModel AB3 = new RendererModel(this, 0, 0);
        AB3.addBox(7.0F, 12.0F, 1.0F, 6, 6, 6);
        RendererModel AC3 = new RendererModel(this, 0, 0);
        AC3.addBox(13.0F, 12.0F, 1.0F, 6, 6, 6);
        RendererModel AD3 = new RendererModel(this, 0, 0);
        AD3.addBox(19.0F, 12.0F, 1.0F, 6, 6, 6);
        RendererModel AE3 = new RendererModel(this, 0, 0);
        AE3.addBox(25.0F, 12.0F, 1.0F, 6, 6, 6);
        RendererModel BA3 = new RendererModel(this, 0, 0);
        BA3.addBox(1.0F, 12.0F, 7.0F, 6, 6, 6);
        RendererModel BB3 = new RendererModel(this, 0, 0);
        BB3.addBox(7.0F, 12.0F, 7.0F, 6, 6, 6);
        RendererModel BC3 = new RendererModel(this, 0, 0);
        BC3.addBox(13.0F, 12.0F, 7.0F, 6, 6, 6);
        RendererModel BD3 = new RendererModel(this, 0, 0);
        BD3.addBox(19.0F, 12.0F, 7.0F, 6, 6, 6);
        RendererModel BE3 = new RendererModel(this, 0, 0);
        BE3.addBox(25.0F, 12.0F, 7.0F, 6, 6, 6);
        RendererModel CA3 = new RendererModel(this, 0, 0);
        CA3.addBox(1.0F, 12.0F, 13.0F, 6, 6, 6);
        RendererModel CB3 = new RendererModel(this, 0, 0);
        CB3.addBox(7.0F, 12.0F, 13.0F, 6, 6, 6);
        RendererModel CC3 = new RendererModel(this, 0, 0);
        CC3.addBox(13.0F, 12.0F, 13.0F, 6, 6, 6);
        RendererModel CD3 = new RendererModel(this, 0, 0);
        CD3.addBox(19.0F, 12.0F, 13.0F, 6, 6, 6);
        RendererModel CE3 = new RendererModel(this, 0, 0);
        CE3.addBox(25.0F, 12.0F, 13.0F, 6, 6, 6);
        RendererModel DA3 = new RendererModel(this, 0, 0);
        DA3.addBox(1.0F, 12.0F, 19.0F, 6, 6, 6);
        RendererModel DB3 = new RendererModel(this, 0, 0);
        DB3.addBox(7.0F, 12.0F, 19.0F, 6, 6, 6);
        RendererModel DC3 = new RendererModel(this, 0, 0);
        DC3.addBox(13.0F, 12.0F, 19.0F, 6, 6, 6);
        RendererModel DD3 = new RendererModel(this, 0, 0);
        DD3.addBox(19.0F, 12.0F, 19.0F, 6, 6, 6);
        RendererModel DE3 = new RendererModel(this, 0, 0);
        DE3.addBox(25.0F, 12.0F, 19.0F, 6, 6, 6);
        RendererModel EA3 = new RendererModel(this, 0, 0);
        EA3.addBox(1.0F, 12.0F, 25.0F, 6, 6, 6);
        RendererModel EB3 = new RendererModel(this, 0, 0);
        EB3.addBox(7.0F, 12.0F, 25.0F, 6, 6, 6);
        RendererModel EC3 = new RendererModel(this, 0, 0);
        EC3.addBox(13.0F, 12.0F, 25.0F, 6, 6, 6);
        RendererModel ED3 = new RendererModel(this, 0, 0);
        ED3.addBox(19.0F, 12.0F, 25.0F, 6, 6, 6);
        RendererModel EE3 = new RendererModel(this, 0, 0);
        EE3.addBox(25.0F, 12.0F, 25.0F, 6, 6, 6);
        RendererModel AA4 = new RendererModel(this, 0, 0);
        AA4.addBox(1.0F, 18.0F, 1.0F, 6, 6, 6);
        RendererModel AB4 = new RendererModel(this, 0, 0);
        AB4.addBox(7.0F, 18.0F, 1.0F, 6, 6, 6);
        RendererModel AC4 = new RendererModel(this, 0, 0);
        AC4.addBox(13.0F, 18.0F, 1.0F, 6, 6, 6);
        RendererModel AD4 = new RendererModel(this, 0, 0);
        AD4.addBox(19.0F, 18.0F, 1.0F, 6, 6, 6);
        RendererModel AE4 = new RendererModel(this, 0, 0);
        AE4.addBox(25.0F, 18.0F, 1.0F, 6, 6, 6);
        RendererModel BA4 = new RendererModel(this, 0, 0);
        BA4.addBox(1.0F, 18.0F, 7.0F, 6, 6, 6);
        RendererModel BB4 = new RendererModel(this, 0, 0);
        BB4.addBox(7.0F, 18.0F, 7.0F, 6, 6, 6);
        RendererModel BC4 = new RendererModel(this, 0, 0);
        BC4.addBox(13.0F, 18.0F, 7.0F, 6, 6, 6);
        RendererModel BD4 = new RendererModel(this, 0, 0);
        BD4.addBox(19.0F, 18.0F, 7.0F, 6, 6, 6);
        RendererModel BE4 = new RendererModel(this, 0, 0);
        BE4.addBox(25.0F, 18.0F, 7.0F, 6, 6, 6);
        RendererModel CA4 = new RendererModel(this, 0, 0);
        CA4.addBox(1.0F, 18.0F, 13.0F, 6, 6, 6);
        RendererModel CB4 = new RendererModel(this, 0, 0);
        CB4.addBox(7.0F, 18.0F, 13.0F, 6, 6, 6);
        RendererModel CC4 = new RendererModel(this, 0, 0);
        CC4.addBox(13.0F, 18.0F, 13.0F, 6, 6, 6);
        RendererModel CD4 = new RendererModel(this, 0, 0);
        CD4.addBox(19.0F, 18.0F, 13.0F, 6, 6, 6);
        RendererModel CE4 = new RendererModel(this, 0, 0);
        CE4.addBox(25.0F, 18.0F, 13.0F, 6, 6, 6);
        RendererModel DA4 = new RendererModel(this, 0, 0);
        DA4.addBox(1.0F, 18.0F, 19.0F, 6, 6, 6);
        RendererModel DB4 = new RendererModel(this, 0, 0);
        DB4.addBox(7.0F, 18.0F, 19.0F, 6, 6, 6);
        RendererModel DC4 = new RendererModel(this, 0, 0);
        DC4.addBox(13.0F, 18.0F, 19.0F, 6, 6, 6);
        RendererModel DD4 = new RendererModel(this, 0, 0);
        DD4.addBox(19.0F, 18.0F, 19.0F, 6, 6, 6);
        RendererModel DE4 = new RendererModel(this, 0, 0);
        DE4.addBox(25.0F, 18.0F, 19.0F, 6, 6, 6);
        RendererModel EA4 = new RendererModel(this, 0, 0);
        EA4.addBox(1.0F, 18.0F, 25.0F, 6, 6, 6);
        RendererModel EB4 = new RendererModel(this, 0, 0);
        EB4.addBox(7.0F, 18.0F, 25.0F, 6, 6, 6);
        RendererModel EC4 = new RendererModel(this, 0, 0);
        EC4.addBox(13.0F, 18.0F, 25.0F, 6, 6, 6);
        RendererModel ED4 = new RendererModel(this, 0, 0);
        ED4.addBox(19.0F, 18.0F, 25.0F, 6, 6, 6);
        RendererModel EE4 = new RendererModel(this, 0, 0);
        EE4.addBox(25.0F, 18.0F, 25.0F, 6, 6, 6);
        RendererModel AA5 = new RendererModel(this, 0, 0);
        AA5.addBox(1.0F, 24.0F, 1.0F, 6, 6, 6);
        RendererModel AB5 = new RendererModel(this, 0, 0);
        AB5.addBox(7.0F, 24.0F, 1.0F, 6, 6, 6);
        RendererModel AC5 = new RendererModel(this, 0, 0);
        AC5.addBox(13.0F, 24.0F, 1.0F, 6, 6, 6);
        RendererModel AD5 = new RendererModel(this, 0, 0);
        AD5.addBox(19.0F, 24.0F, 1.0F, 6, 6, 6);
        RendererModel AE5 = new RendererModel(this, 0, 0);
        AE5.addBox(25.0F, 24.0F, 1.0F, 6, 6, 6);
        RendererModel BA5 = new RendererModel(this, 0, 0);
        BA5.addBox(1.0F, 24.0F, 7.0F, 6, 6, 6);
        RendererModel BB5 = new RendererModel(this, 0, 0);
        BB5.addBox(7.0F, 24.0F, 7.0F, 6, 6, 6);
        RendererModel BC5 = new RendererModel(this, 0, 0);
        BC5.addBox(13.0F, 24.0F, 7.0F, 6, 6, 6);
        RendererModel BD5 = new RendererModel(this, 0, 0);
        BD5.addBox(19.0F, 24.0F, 7.0F, 6, 6, 6);
        RendererModel BE5 = new RendererModel(this, 0, 0);
        BE5.addBox(25.0F, 24.0F, 7.0F, 6, 6, 6);
        RendererModel CA5 = new RendererModel(this, 0, 0);
        CA5.addBox(1.0F, 24.0F, 13.0F, 6, 6, 6);
        RendererModel CB5 = new RendererModel(this, 0, 0);
        CB5.addBox(7.0F, 24.0F, 13.0F, 6, 6, 6);
        RendererModel CC5 = new RendererModel(this, 0, 0);
        CC5.addBox(13.0F, 24.0F, 13.0F, 6, 6, 6);
        RendererModel CD5 = new RendererModel(this, 0, 0);
        CD5.addBox(19.0F, 24.0F, 13.0F, 6, 6, 6);
        RendererModel CE5 = new RendererModel(this, 0, 0);
        CE5.addBox(25.0F, 24.0F, 13.0F, 6, 6, 6);
        RendererModel DA5 = new RendererModel(this, 0, 0);
        DA5.addBox(1.0F, 24.0F, 19.0F, 6, 6, 6);
        RendererModel DB5 = new RendererModel(this, 0, 0);
        DB5.addBox(7.0F, 24.0F, 19.0F, 6, 6, 6);
        RendererModel DC5 = new RendererModel(this, 0, 0);
        DC5.addBox(13.0F, 24.0F, 19.0F, 6, 6, 6);
        RendererModel DD5 = new RendererModel(this, 0, 0);
        DD5.addBox(19.0F, 24.0F, 19.0F, 6, 6, 6);
        RendererModel DE5 = new RendererModel(this, 0, 0);
        DE5.addBox(25.0F, 24.0F, 19.0F, 6, 6, 6);
        RendererModel EA5 = new RendererModel(this, 0, 0);
        EA5.addBox(1.0F, 24.0F, 25.0F, 6, 6, 6);
        RendererModel EB5 = new RendererModel(this, 0, 0);
        EB5.addBox(7.0F, 24.0F, 25.0F, 6, 6, 6);
        RendererModel EC5 = new RendererModel(this, 0, 0);
        EC5.addBox(13.0F, 24.0F, 25.0F, 6, 6, 6);
        RendererModel ED5 = new RendererModel(this, 0, 0);
        ED5.addBox(19.0F, 24.0F, 25.0F, 6, 6, 6);
        RendererModel EE5 = new RendererModel(this, 0, 0);
        EE5.addBox(25.0F, 24.0F, 25.0F, 6, 6, 6);
        this.layer1 = new RendererModel[][]{{AA1, BA1, CA1, DA1, EA1},
                                            {AB1, BB1, CB1, DB1, EB1},
                                            {AC1, BC1, CC1, DC1, EC1},
                                            {AD1, BD1, CD1, DD1, ED1},
                                            {AE1, BE1, CE1, DE1, EE1}};
        this.layer2 = new RendererModel[][]{{AA2, BA2, CA2, DA2, EA2},
                                            {AB2, BB2, CB2, DB2, EB2},
                                            {AC2, BC2, CC2, DC2, EC2},
                                            {AD2, BD2, CD2, DD2, ED2},
                                            {AE2, BE2, CE2, DE2, EE2}};
        this.layer3 = new RendererModel[][]{{AA3, BA3, CA3, DA3, EA3},
                                            {AB3, BB3, CB3, DB3, EB3},
                                            {AC3, BC3, CC3, DC3, EC3},
                                            {AD3, BD3, CD3, DD3, ED3},
                                            {AE3, BE3, CE3, DE3, EE3}};
        this.layer4 = new RendererModel[][]{{AA4, BA4, CA4, DA4, EA4},
                                            {AB4, BB4, CB4, DB4, EB4},
                                            {AC4, BC4, CC4, DC4, EC4},
                                            {AD4, BD4, CD4, DD4, ED4},
                                            {AE4, BE4, CE4, DE4, EE4}};
        this.layer5 = new RendererModel[][]{{AA5, BA5, CA5, DA5, EA5},
                                            {AB5, BB5, CB5, DB5, EB5},
                                            {AC5, BC5, CC5, DC5, EC5},
                                            {AD5, BD5, CD5, DD5, ED5},
                                            {AE5, BE5, CE5, DE5, EE5}};
        this.layers = new RendererModel[][][]{this.layer1, this.layer2, this.layer3, this.layer4, this.layer5};
    }

    public void renderBase() {
        this.base.render(1.0F / 32.0F);
        this.baseN.render(1.0F / 32.0F);
        this.baseS.render(1.0F / 32.0F);
        this.baseW.render(1.0F / 32.0F);
        this.baseE.render(1.0F / 32.0F);
    }

    public void renderLayer(boolean[][] matrix, int index) {
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                if (matrix[i][j]) {
                    this.layers[index][i][j].render(1.0F / 32.0F);
                }
            }
        }
    }
}
