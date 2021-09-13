package tgw.evolution.client.models.entities;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;
import tgw.evolution.util.MathHelper;

public class ModelHook extends Model {

    private final ModelRenderer bone;

    public ModelHook() {
        super(RenderType::entitySolid);
        this.texWidth = 16;
        this.texHeight = 16;
        this.bone = new ModelRenderer(this, 0, 0);
        this.bone.setPos(0.0F, 24.0F, 0.0F);
        MathHelper.setRotationAngle(this.bone, -MathHelper.PI_OVER_2, 0.0F, 0.0F);
        this.bone.addBox("", 2.5f, 2.0f, -23.0f, 1, 1, 3, 0.0f, 8, 0);
        this.bone.addBox("", -3.5f, 2.0F, -23.0F, 1, 1, 3, 0.0F, 8, 4);
        this.bone.addBox("", 1.5F, 1.0F, -24.0F, 1, 1, 1, 0.0F, 0, 7);
        this.bone.addBox("", -2.5F, 1.0F, -24.0F, 1, 1, 1, 0.0F, 5, 0);
        this.bone.addBox("", -1.5F, 0.0F, -25.0F, 1, 1, 1, 0.0F, 0, 5);
        this.bone.addBox("", 0.5F, 0.0F, -25.0F, 1, 1, 1, 0.0F, 0, 9);
        this.bone.addBox("", -0.5F, -1.0F, -25.0F, 1, 1, 1, 0.0F, 0, 11);
        this.bone.addBox("", -0.5F, -2.0F, -24.0F, 1, 1, 1, 0.0F, 0, 13);
        this.bone.addBox("", -0.5F, -3.0F, -23.0F, 1, 1, 3, 0.0F, 0, 0);
        this.bone.addBox("", -0.5F, -0.5F, -24.0F, 1, 1, 7, 0.0F, 0, 8);
    }

    @Override
    public void renderToBuffer(MatrixStack matrices,
                               IVertexBuilder vertex,
                               int packedLight,
                               int packedOverlay,
                               float red,
                               float green,
                               float blue,
                               float alpha) {
        this.bone.render(matrices, vertex, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
