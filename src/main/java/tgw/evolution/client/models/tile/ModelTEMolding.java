package tgw.evolution.client.models.tile;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;
import tgw.evolution.util.MathHelper;

public class ModelTEMolding extends Model {

    public final ModelRenderer[][] layer1;
    public final ModelRenderer[][] layer2;
    public final ModelRenderer[][] layer3;
    public final ModelRenderer[][] layer4;
    public final ModelRenderer[][] layer5;
    private final ModelRenderer base;
    private final ModelRenderer baseE;
    private final ModelRenderer baseN;
    private final ModelRenderer baseS;
    private final ModelRenderer baseW;
    private final ModelRenderer[][][] layers;

    public ModelTEMolding() {
        super(RenderType::entitySolid);
        super.texWidth = 128;
        super.texHeight = 40;
        this.base = new ModelRenderer(this, 0, 0);
        this.base.addBox(0.0F, 0.0F, 0.0F, 32, 1, 32);
        this.baseN = new ModelRenderer(this, 0, 33);
        this.baseN.addBox(0.0F, 1.0F, 0.0F, 32, 5, 1);
        this.baseS = new ModelRenderer(this, 0, 33);
        this.baseS.addBox(0.0F, 1.0F, 31.0F, 32, 5, 1);
        this.baseW = new ModelRenderer(this, 66, 33);
        this.baseW.addBox(-31.0F, 1.0F, 0.0F, 30, 5, 1);
        MathHelper.setRotationAngle(this.baseW, MathHelper.PI_OVER_2, MathHelper.PI_OVER_2, MathHelper.PI_OVER_2);
        this.baseE = new ModelRenderer(this, 66, 33);
        this.baseE.addBox(-31.0F, 1.0F, 31.0F, 30, 5, 1);
        MathHelper.setRotationAngle(this.baseE, MathHelper.PI_OVER_2, MathHelper.PI_OVER_2, MathHelper.PI_OVER_2);
        ModelRenderer AA1 = new ModelRenderer(this, 0, 0);
        AA1.addBox(1.0F, 0.0F, 1.0F, 6, 6, 6);
        ModelRenderer AB1 = new ModelRenderer(this, 0, 0);
        AB1.addBox(7.0F, 0.0F, 1.0F, 6, 6, 6);
        ModelRenderer AC1 = new ModelRenderer(this, 0, 0);
        AC1.addBox(13.0F, 0.0F, 1.0F, 6, 6, 6);
        ModelRenderer AD1 = new ModelRenderer(this, 0, 0);
        AD1.addBox(19.0F, 0.0F, 1.0F, 6, 6, 6);
        ModelRenderer AE1 = new ModelRenderer(this, 0, 0);
        AE1.addBox(25.0F, 0.0F, 1.0F, 6, 6, 6);
        ModelRenderer BA1 = new ModelRenderer(this, 0, 0);
        BA1.addBox(1.0F, 0.0F, 7.0F, 6, 6, 6);
        ModelRenderer BB1 = new ModelRenderer(this, 0, 0);
        BB1.addBox(7.0F, 0.0F, 7.0F, 6, 6, 6);
        ModelRenderer BC1 = new ModelRenderer(this, 0, 0);
        BC1.addBox(13.0F, 0.0F, 7.0F, 6, 6, 6);
        ModelRenderer BD1 = new ModelRenderer(this, 0, 0);
        BD1.addBox(19.0F, 0.0F, 7.0F, 6, 6, 6);
        ModelRenderer BE1 = new ModelRenderer(this, 0, 0);
        BE1.addBox(25.0F, 0.0F, 7.0F, 6, 6, 6);
        ModelRenderer CA1 = new ModelRenderer(this, 0, 0);
        CA1.addBox(1.0F, 0.0F, 13.0F, 6, 6, 6);
        ModelRenderer CB1 = new ModelRenderer(this, 0, 0);
        CB1.addBox(7.0F, 0.0F, 13.0F, 6, 6, 6);
        ModelRenderer CC1 = new ModelRenderer(this, 0, 0);
        CC1.addBox(13.0F, 0.0F, 13.0F, 6, 6, 6);
        ModelRenderer CD1 = new ModelRenderer(this, 0, 0);
        CD1.addBox(19.0F, 0.0F, 13.0F, 6, 6, 6);
        ModelRenderer CE1 = new ModelRenderer(this, 0, 0);
        CE1.addBox(25.0F, 0.0F, 13.0F, 6, 6, 6);
        ModelRenderer DA1 = new ModelRenderer(this, 0, 0);
        DA1.addBox(1.0F, 0.0F, 19.0F, 6, 6, 6);
        ModelRenderer DB1 = new ModelRenderer(this, 0, 0);
        DB1.addBox(7.0F, 0.0F, 19.0F, 6, 6, 6);
        ModelRenderer DC1 = new ModelRenderer(this, 0, 0);
        DC1.addBox(13.0F, 0.0F, 19.0F, 6, 6, 6);
        ModelRenderer DD1 = new ModelRenderer(this, 0, 0);
        DD1.addBox(19.0F, 0.0F, 19.0F, 6, 6, 6);
        ModelRenderer DE1 = new ModelRenderer(this, 0, 0);
        DE1.addBox(25.0F, 0.0F, 19.0F, 6, 6, 6);
        ModelRenderer EA1 = new ModelRenderer(this, 0, 0);
        EA1.addBox(1.0F, 0.0F, 25.0F, 6, 6, 6);
        ModelRenderer EB1 = new ModelRenderer(this, 0, 0);
        EB1.addBox(7.0F, 0.0F, 25.0F, 6, 6, 6);
        ModelRenderer EC1 = new ModelRenderer(this, 0, 0);
        EC1.addBox(13.0F, 0.0F, 25.0F, 6, 6, 6);
        ModelRenderer ED1 = new ModelRenderer(this, 0, 0);
        ED1.addBox(19.0F, 0.0F, 25.0F, 6, 6, 6);
        ModelRenderer EE1 = new ModelRenderer(this, 0, 0);
        EE1.addBox(25.0F, 0.0F, 25.0F, 6, 6, 6);
        ModelRenderer AA2 = new ModelRenderer(this, 0, 0);
        AA2.addBox(1.0F, 6.0F, 1.0F, 6, 6, 6);
        ModelRenderer AB2 = new ModelRenderer(this, 0, 0);
        AB2.addBox(7.0F, 6.0F, 1.0F, 6, 6, 6);
        ModelRenderer AC2 = new ModelRenderer(this, 0, 0);
        AC2.addBox(13.0F, 6.0F, 1.0F, 6, 6, 6);
        ModelRenderer AD2 = new ModelRenderer(this, 0, 0);
        AD2.addBox(19.0F, 6.0F, 1.0F, 6, 6, 6);
        ModelRenderer AE2 = new ModelRenderer(this, 0, 0);
        AE2.addBox(25.0F, 6.0F, 1.0F, 6, 6, 6);
        ModelRenderer BA2 = new ModelRenderer(this, 0, 0);
        BA2.addBox(1.0F, 6.0F, 7.0F, 6, 6, 6);
        ModelRenderer BB2 = new ModelRenderer(this, 0, 0);
        BB2.addBox(7.0F, 6.0F, 7.0F, 6, 6, 6);
        ModelRenderer BC2 = new ModelRenderer(this, 0, 0);
        BC2.addBox(13.0F, 6.0F, 7.0F, 6, 6, 6);
        ModelRenderer BD2 = new ModelRenderer(this, 0, 0);
        BD2.addBox(19.0F, 6.0F, 7.0F, 6, 6, 6);
        ModelRenderer BE2 = new ModelRenderer(this, 0, 0);
        BE2.addBox(25.0F, 6.0F, 7.0F, 6, 6, 6);
        ModelRenderer CA2 = new ModelRenderer(this, 0, 0);
        CA2.addBox(1.0F, 6.0F, 13.0F, 6, 6, 6);
        ModelRenderer CB2 = new ModelRenderer(this, 0, 0);
        CB2.addBox(7.0F, 6.0F, 13.0F, 6, 6, 6);
        ModelRenderer CC2 = new ModelRenderer(this, 0, 0);
        CC2.addBox(13.0F, 6.0F, 13.0F, 6, 6, 6);
        ModelRenderer CD2 = new ModelRenderer(this, 0, 0);
        CD2.addBox(19.0F, 6.0F, 13.0F, 6, 6, 6);
        ModelRenderer CE2 = new ModelRenderer(this, 0, 0);
        CE2.addBox(25.0F, 6.0F, 13.0F, 6, 6, 6);
        ModelRenderer DA2 = new ModelRenderer(this, 0, 0);
        DA2.addBox(1.0F, 6.0F, 19.0F, 6, 6, 6);
        ModelRenderer DB2 = new ModelRenderer(this, 0, 0);
        DB2.addBox(7.0F, 6.0F, 19.0F, 6, 6, 6);
        ModelRenderer DC2 = new ModelRenderer(this, 0, 0);
        DC2.addBox(13.0F, 6.0F, 19.0F, 6, 6, 6);
        ModelRenderer DD2 = new ModelRenderer(this, 0, 0);
        DD2.addBox(19.0F, 6.0F, 19.0F, 6, 6, 6);
        ModelRenderer DE2 = new ModelRenderer(this, 0, 0);
        DE2.addBox(25.0F, 6.0F, 19.0F, 6, 6, 6);
        ModelRenderer EA2 = new ModelRenderer(this, 0, 0);
        EA2.addBox(1.0F, 6.0F, 25.0F, 6, 6, 6);
        ModelRenderer EB2 = new ModelRenderer(this, 0, 0);
        EB2.addBox(7.0F, 6.0F, 25.0F, 6, 6, 6);
        ModelRenderer EC2 = new ModelRenderer(this, 0, 0);
        EC2.addBox(13.0F, 6.0F, 25.0F, 6, 6, 6);
        ModelRenderer ED2 = new ModelRenderer(this, 0, 0);
        ED2.addBox(19.0F, 6.0F, 25.0F, 6, 6, 6);
        ModelRenderer EE2 = new ModelRenderer(this, 0, 0);
        EE2.addBox(25.0F, 6.0F, 25.0F, 6, 6, 6);
        ModelRenderer AA3 = new ModelRenderer(this, 0, 0);
        AA3.addBox(1.0F, 12.0F, 1.0F, 6, 6, 6);
        ModelRenderer AB3 = new ModelRenderer(this, 0, 0);
        AB3.addBox(7.0F, 12.0F, 1.0F, 6, 6, 6);
        ModelRenderer AC3 = new ModelRenderer(this, 0, 0);
        AC3.addBox(13.0F, 12.0F, 1.0F, 6, 6, 6);
        ModelRenderer AD3 = new ModelRenderer(this, 0, 0);
        AD3.addBox(19.0F, 12.0F, 1.0F, 6, 6, 6);
        ModelRenderer AE3 = new ModelRenderer(this, 0, 0);
        AE3.addBox(25.0F, 12.0F, 1.0F, 6, 6, 6);
        ModelRenderer BA3 = new ModelRenderer(this, 0, 0);
        BA3.addBox(1.0F, 12.0F, 7.0F, 6, 6, 6);
        ModelRenderer BB3 = new ModelRenderer(this, 0, 0);
        BB3.addBox(7.0F, 12.0F, 7.0F, 6, 6, 6);
        ModelRenderer BC3 = new ModelRenderer(this, 0, 0);
        BC3.addBox(13.0F, 12.0F, 7.0F, 6, 6, 6);
        ModelRenderer BD3 = new ModelRenderer(this, 0, 0);
        BD3.addBox(19.0F, 12.0F, 7.0F, 6, 6, 6);
        ModelRenderer BE3 = new ModelRenderer(this, 0, 0);
        BE3.addBox(25.0F, 12.0F, 7.0F, 6, 6, 6);
        ModelRenderer CA3 = new ModelRenderer(this, 0, 0);
        CA3.addBox(1.0F, 12.0F, 13.0F, 6, 6, 6);
        ModelRenderer CB3 = new ModelRenderer(this, 0, 0);
        CB3.addBox(7.0F, 12.0F, 13.0F, 6, 6, 6);
        ModelRenderer CC3 = new ModelRenderer(this, 0, 0);
        CC3.addBox(13.0F, 12.0F, 13.0F, 6, 6, 6);
        ModelRenderer CD3 = new ModelRenderer(this, 0, 0);
        CD3.addBox(19.0F, 12.0F, 13.0F, 6, 6, 6);
        ModelRenderer CE3 = new ModelRenderer(this, 0, 0);
        CE3.addBox(25.0F, 12.0F, 13.0F, 6, 6, 6);
        ModelRenderer DA3 = new ModelRenderer(this, 0, 0);
        DA3.addBox(1.0F, 12.0F, 19.0F, 6, 6, 6);
        ModelRenderer DB3 = new ModelRenderer(this, 0, 0);
        DB3.addBox(7.0F, 12.0F, 19.0F, 6, 6, 6);
        ModelRenderer DC3 = new ModelRenderer(this, 0, 0);
        DC3.addBox(13.0F, 12.0F, 19.0F, 6, 6, 6);
        ModelRenderer DD3 = new ModelRenderer(this, 0, 0);
        DD3.addBox(19.0F, 12.0F, 19.0F, 6, 6, 6);
        ModelRenderer DE3 = new ModelRenderer(this, 0, 0);
        DE3.addBox(25.0F, 12.0F, 19.0F, 6, 6, 6);
        ModelRenderer EA3 = new ModelRenderer(this, 0, 0);
        EA3.addBox(1.0F, 12.0F, 25.0F, 6, 6, 6);
        ModelRenderer EB3 = new ModelRenderer(this, 0, 0);
        EB3.addBox(7.0F, 12.0F, 25.0F, 6, 6, 6);
        ModelRenderer EC3 = new ModelRenderer(this, 0, 0);
        EC3.addBox(13.0F, 12.0F, 25.0F, 6, 6, 6);
        ModelRenderer ED3 = new ModelRenderer(this, 0, 0);
        ED3.addBox(19.0F, 12.0F, 25.0F, 6, 6, 6);
        ModelRenderer EE3 = new ModelRenderer(this, 0, 0);
        EE3.addBox(25.0F, 12.0F, 25.0F, 6, 6, 6);
        ModelRenderer AA4 = new ModelRenderer(this, 0, 0);
        AA4.addBox(1.0F, 18.0F, 1.0F, 6, 6, 6);
        ModelRenderer AB4 = new ModelRenderer(this, 0, 0);
        AB4.addBox(7.0F, 18.0F, 1.0F, 6, 6, 6);
        ModelRenderer AC4 = new ModelRenderer(this, 0, 0);
        AC4.addBox(13.0F, 18.0F, 1.0F, 6, 6, 6);
        ModelRenderer AD4 = new ModelRenderer(this, 0, 0);
        AD4.addBox(19.0F, 18.0F, 1.0F, 6, 6, 6);
        ModelRenderer AE4 = new ModelRenderer(this, 0, 0);
        AE4.addBox(25.0F, 18.0F, 1.0F, 6, 6, 6);
        ModelRenderer BA4 = new ModelRenderer(this, 0, 0);
        BA4.addBox(1.0F, 18.0F, 7.0F, 6, 6, 6);
        ModelRenderer BB4 = new ModelRenderer(this, 0, 0);
        BB4.addBox(7.0F, 18.0F, 7.0F, 6, 6, 6);
        ModelRenderer BC4 = new ModelRenderer(this, 0, 0);
        BC4.addBox(13.0F, 18.0F, 7.0F, 6, 6, 6);
        ModelRenderer BD4 = new ModelRenderer(this, 0, 0);
        BD4.addBox(19.0F, 18.0F, 7.0F, 6, 6, 6);
        ModelRenderer BE4 = new ModelRenderer(this, 0, 0);
        BE4.addBox(25.0F, 18.0F, 7.0F, 6, 6, 6);
        ModelRenderer CA4 = new ModelRenderer(this, 0, 0);
        CA4.addBox(1.0F, 18.0F, 13.0F, 6, 6, 6);
        ModelRenderer CB4 = new ModelRenderer(this, 0, 0);
        CB4.addBox(7.0F, 18.0F, 13.0F, 6, 6, 6);
        ModelRenderer CC4 = new ModelRenderer(this, 0, 0);
        CC4.addBox(13.0F, 18.0F, 13.0F, 6, 6, 6);
        ModelRenderer CD4 = new ModelRenderer(this, 0, 0);
        CD4.addBox(19.0F, 18.0F, 13.0F, 6, 6, 6);
        ModelRenderer CE4 = new ModelRenderer(this, 0, 0);
        CE4.addBox(25.0F, 18.0F, 13.0F, 6, 6, 6);
        ModelRenderer DA4 = new ModelRenderer(this, 0, 0);
        DA4.addBox(1.0F, 18.0F, 19.0F, 6, 6, 6);
        ModelRenderer DB4 = new ModelRenderer(this, 0, 0);
        DB4.addBox(7.0F, 18.0F, 19.0F, 6, 6, 6);
        ModelRenderer DC4 = new ModelRenderer(this, 0, 0);
        DC4.addBox(13.0F, 18.0F, 19.0F, 6, 6, 6);
        ModelRenderer DD4 = new ModelRenderer(this, 0, 0);
        DD4.addBox(19.0F, 18.0F, 19.0F, 6, 6, 6);
        ModelRenderer DE4 = new ModelRenderer(this, 0, 0);
        DE4.addBox(25.0F, 18.0F, 19.0F, 6, 6, 6);
        ModelRenderer EA4 = new ModelRenderer(this, 0, 0);
        EA4.addBox(1.0F, 18.0F, 25.0F, 6, 6, 6);
        ModelRenderer EB4 = new ModelRenderer(this, 0, 0);
        EB4.addBox(7.0F, 18.0F, 25.0F, 6, 6, 6);
        ModelRenderer EC4 = new ModelRenderer(this, 0, 0);
        EC4.addBox(13.0F, 18.0F, 25.0F, 6, 6, 6);
        ModelRenderer ED4 = new ModelRenderer(this, 0, 0);
        ED4.addBox(19.0F, 18.0F, 25.0F, 6, 6, 6);
        ModelRenderer EE4 = new ModelRenderer(this, 0, 0);
        EE4.addBox(25.0F, 18.0F, 25.0F, 6, 6, 6);
        ModelRenderer AA5 = new ModelRenderer(this, 0, 0);
        AA5.addBox(1.0F, 24.0F, 1.0F, 6, 6, 6);
        ModelRenderer AB5 = new ModelRenderer(this, 0, 0);
        AB5.addBox(7.0F, 24.0F, 1.0F, 6, 6, 6);
        ModelRenderer AC5 = new ModelRenderer(this, 0, 0);
        AC5.addBox(13.0F, 24.0F, 1.0F, 6, 6, 6);
        ModelRenderer AD5 = new ModelRenderer(this, 0, 0);
        AD5.addBox(19.0F, 24.0F, 1.0F, 6, 6, 6);
        ModelRenderer AE5 = new ModelRenderer(this, 0, 0);
        AE5.addBox(25.0F, 24.0F, 1.0F, 6, 6, 6);
        ModelRenderer BA5 = new ModelRenderer(this, 0, 0);
        BA5.addBox(1.0F, 24.0F, 7.0F, 6, 6, 6);
        ModelRenderer BB5 = new ModelRenderer(this, 0, 0);
        BB5.addBox(7.0F, 24.0F, 7.0F, 6, 6, 6);
        ModelRenderer BC5 = new ModelRenderer(this, 0, 0);
        BC5.addBox(13.0F, 24.0F, 7.0F, 6, 6, 6);
        ModelRenderer BD5 = new ModelRenderer(this, 0, 0);
        BD5.addBox(19.0F, 24.0F, 7.0F, 6, 6, 6);
        ModelRenderer BE5 = new ModelRenderer(this, 0, 0);
        BE5.addBox(25.0F, 24.0F, 7.0F, 6, 6, 6);
        ModelRenderer CA5 = new ModelRenderer(this, 0, 0);
        CA5.addBox(1.0F, 24.0F, 13.0F, 6, 6, 6);
        ModelRenderer CB5 = new ModelRenderer(this, 0, 0);
        CB5.addBox(7.0F, 24.0F, 13.0F, 6, 6, 6);
        ModelRenderer CC5 = new ModelRenderer(this, 0, 0);
        CC5.addBox(13.0F, 24.0F, 13.0F, 6, 6, 6);
        ModelRenderer CD5 = new ModelRenderer(this, 0, 0);
        CD5.addBox(19.0F, 24.0F, 13.0F, 6, 6, 6);
        ModelRenderer CE5 = new ModelRenderer(this, 0, 0);
        CE5.addBox(25.0F, 24.0F, 13.0F, 6, 6, 6);
        ModelRenderer DA5 = new ModelRenderer(this, 0, 0);
        DA5.addBox(1.0F, 24.0F, 19.0F, 6, 6, 6);
        ModelRenderer DB5 = new ModelRenderer(this, 0, 0);
        DB5.addBox(7.0F, 24.0F, 19.0F, 6, 6, 6);
        ModelRenderer DC5 = new ModelRenderer(this, 0, 0);
        DC5.addBox(13.0F, 24.0F, 19.0F, 6, 6, 6);
        ModelRenderer DD5 = new ModelRenderer(this, 0, 0);
        DD5.addBox(19.0F, 24.0F, 19.0F, 6, 6, 6);
        ModelRenderer DE5 = new ModelRenderer(this, 0, 0);
        DE5.addBox(25.0F, 24.0F, 19.0F, 6, 6, 6);
        ModelRenderer EA5 = new ModelRenderer(this, 0, 0);
        EA5.addBox(1.0F, 24.0F, 25.0F, 6, 6, 6);
        ModelRenderer EB5 = new ModelRenderer(this, 0, 0);
        EB5.addBox(7.0F, 24.0F, 25.0F, 6, 6, 6);
        ModelRenderer EC5 = new ModelRenderer(this, 0, 0);
        EC5.addBox(13.0F, 24.0F, 25.0F, 6, 6, 6);
        ModelRenderer ED5 = new ModelRenderer(this, 0, 0);
        ED5.addBox(19.0F, 24.0F, 25.0F, 6, 6, 6);
        ModelRenderer EE5 = new ModelRenderer(this, 0, 0);
        EE5.addBox(25.0F, 24.0F, 25.0F, 6, 6, 6);
        this.layer1 = new ModelRenderer[][]{{AA1, BA1, CA1, DA1, EA1},
                                            {AB1, BB1, CB1, DB1, EB1},
                                            {AC1, BC1, CC1, DC1, EC1},
                                            {AD1, BD1, CD1, DD1, ED1},
                                            {AE1, BE1, CE1, DE1, EE1}};
        this.layer2 = new ModelRenderer[][]{{AA2, BA2, CA2, DA2, EA2},
                                            {AB2, BB2, CB2, DB2, EB2},
                                            {AC2, BC2, CC2, DC2, EC2},
                                            {AD2, BD2, CD2, DD2, ED2},
                                            {AE2, BE2, CE2, DE2, EE2}};
        this.layer3 = new ModelRenderer[][]{{AA3, BA3, CA3, DA3, EA3},
                                            {AB3, BB3, CB3, DB3, EB3},
                                            {AC3, BC3, CC3, DC3, EC3},
                                            {AD3, BD3, CD3, DD3, ED3},
                                            {AE3, BE3, CE3, DE3, EE3}};
        this.layer4 = new ModelRenderer[][]{{AA4, BA4, CA4, DA4, EA4},
                                            {AB4, BB4, CB4, DB4, EB4},
                                            {AC4, BC4, CC4, DC4, EC4},
                                            {AD4, BD4, CD4, DD4, ED4},
                                            {AE4, BE4, CE4, DE4, EE4}};
        this.layer5 = new ModelRenderer[][]{{AA5, BA5, CA5, DA5, EA5},
                                            {AB5, BB5, CB5, DB5, EB5},
                                            {AC5, BC5, CC5, DC5, EC5},
                                            {AD5, BD5, CD5, DD5, ED5},
                                            {AE5, BE5, CE5, DE5, EE5}};
        this.layers = new ModelRenderer[][][]{this.layer1, this.layer2, this.layer3, this.layer4, this.layer5};
    }

    @Override
    public void renderToBuffer(MatrixStack matrices,
                               IVertexBuilder buffer,
                               int packedLight,
                               int packedOverlay,
                               float red,
                               float green,
                               float blue,
                               float alpha) {
        for (ModelRenderer[][] modelRenderers : this.layers) {
            for (ModelRenderer[] modelRenderer : modelRenderers) {
                for (ModelRenderer renderer : modelRenderer) {
                    renderer.render(matrices, buffer, packedLight, packedOverlay, 1.0f, 1.0f, 1.0f, 1.0f);
                }
            }
        }
        this.base.render(matrices, buffer, packedLight, packedOverlay, 1.0f, 1.0f, 1.0f, 1.0f);
        this.baseN.render(matrices, buffer, packedLight, packedOverlay, 1.0f, 1.0f, 1.0f, 1.0f);
        this.baseS.render(matrices, buffer, packedLight, packedOverlay, 1.0f, 1.0f, 1.0f, 1.0f);
        this.baseW.render(matrices, buffer, packedLight, packedOverlay, 1.0f, 1.0f, 1.0f, 1.0f);
        this.baseE.render(matrices, buffer, packedLight, packedOverlay, 1.0f, 1.0f, 1.0f, 1.0f);
    }

    public void setupBase(boolean shouldRender) {
        this.base.visible = shouldRender;
        this.baseN.visible = shouldRender;
        this.baseS.visible = shouldRender;
        this.baseW.visible = shouldRender;
        this.baseE.visible = shouldRender;
    }

    public void setupLayers(boolean[][] matrix, int index) {
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                this.layers[index][i][j].visible = matrix[i][j];
            }
        }
    }
}
