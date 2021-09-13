package tgw.evolution.client.models.entities;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;
import tgw.evolution.util.MathHelper;

public class ModelPlayerCorpse extends Model {

    private final ModelRenderer base;
    private final ModelRenderer overlay;

    public ModelPlayerCorpse() {
        super(RenderType::entityCutoutNoCull);
        this.texWidth = 64;
        this.texHeight = 32;
        this.base = new ModelRenderer(this);
        this.base.setPos(-1.0F, 36.0F, -1.0F);
        MathHelper.setRotationAngle(this.base, -MathHelper.PI_OVER_2, 0.0F, 0.0F);
        this.base.mirror = true;
        this.base.addBox("", 2.0F, 3.0F, -15.0F, 2, 12, 2, 0.0F, 40, 16);
        this.base.mirror = false;
        this.base.addBox("", -3.0F, -17.0F, -18.0F, 8, 8, 8, 0.0F, 0, 0);
        this.base.addBox("", -3.0F, -9.0F, -16.0F, 8, 12, 4, 0.0F, 16, 16);
        this.base.addBox("", -5.0F, -9.0F, -15.0F, 2, 12, 2, 0.0F, 0, 16);
        this.base.mirror = true;
        this.base.addBox("", 5.0F, -9.0F, -15.0F, 2, 12, 2, 0.0F, 0, 16);
        this.base.mirror = false;
        this.base.addBox("", -2.0F, 3.0F, -15.0F, 2, 12, 2, 0.0F, 40, 16);
        this.overlay = new ModelRenderer(this);
    }

    public ModelPlayerCorpse(boolean smallArms) {
        super(RenderType::entityCutoutNoCull);
        this.texWidth = 64;
        this.texHeight = 64;
        this.base = new ModelRenderer(this);
        this.base.setPos(0.0F, 24.0F, 0.0F);
        MathHelper.setRotationAngle(this.base, -MathHelper.PI_OVER_2, 0.0F, 0.0F);
        this.base.addBox("", -4.0f, -16.0f, -6.0f, 8, 8, 8, 0.0F, 0, 0);
        this.base.addBox("", -4.0f, -8.0f, -4.0f, 8, 12, 4, 0.0F, 16, 16);
        this.overlay = new ModelRenderer(this);
        this.overlay.setPos(0.0F, 24.0F, 0.0F);
        MathHelper.setRotationAngle(this.overlay, -MathHelper.PI_OVER_2, 0.0F, 0.0F);
        if (smallArms) {
            this.base.addBox("", -9.0f, -8.0f, -4.0f, 3, 12, 4, 0.0F, 40, 16);
            this.base.addBox("", 4.0F, -8.0f, -4.0f, 3, 12, 4, 0.0F, 32, 48);
            this.overlay.addBox("", -9.0f, -8.0f, -4.0f, 3, 12, 4, 0.25F, 40, 32);
            this.overlay.addBox("", 4.0F, -8.0f, -4.0f, 3, 12, 4, 0.25F, 48, 48);
        }
        else {
            this.base.addBox("", -8.0f, -8.0f, -4.0f, 4, 12, 4, 0.0F, 40, 16);
            this.base.addBox("", 4.0F, -8.0f, -4.0f, 4, 12, 4, 0.0F, 32, 48);
            this.overlay.addBox("", -8.0f, -8.0f, -4.0f, 4, 12, 4, 0.25F, 40, 32);
            this.overlay.addBox("", 4.0F, -8.0f, -4.0f, 4, 12, 4, 0.25F, 48, 48);
        }
        this.base.addBox("", -4.0f, 4.0F, -4.0f, 4, 12, 4, 0.0F, 0, 16);
        this.base.addBox("", 0.0F, 4.0F, -4.0f, 4, 12, 4, 0.0F, 16, 48);
        this.overlay.addBox("", -4.0f, -16.0f, -6.0f, 8, 8, 8, 0.25F, 32, 0);
        this.overlay.addBox("", -4.0f, -8.0f, -4.0f, 8, 12, 4, 0.26F, 16, 32);
        this.overlay.addBox("", -4.0f, 4.0F, -4.0f, 4, 12, 4, 0.25F, 0, 32);
        this.overlay.addBox("", 0.0F, 4.0F, -4.0f, 4, 12, 4, 0.25F, 0, 48);
    }

    @Override
    public void renderToBuffer(MatrixStack matrices,
                               IVertexBuilder vertex,
                               int packetLight,
                               int packetOverlay,
                               float red,
                               float green,
                               float blue,
                               float alpha) {
        this.base.render(matrices, vertex, packetLight, packetOverlay, red, green, blue, alpha);
        this.overlay.render(matrices, vertex, packetLight, packetOverlay, red, green, blue, alpha);
    }
}
