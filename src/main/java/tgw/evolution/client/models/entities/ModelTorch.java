package tgw.evolution.client.models.entities;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelTorch extends Model {

    private final ModelRenderer model;

    public ModelTorch() {
        super(RenderType::entitySolid);
        this.texWidth = 16;
        this.texHeight = 16;
        this.model = new ModelRenderer(this, 0, 0);
        this.model.addBox("", -1.0f, 0.0F, -1.0f, 2, 10, 2, 0.0F, 4, 4);
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
        this.model.render(matrices, vertex, packetLight, packetOverlay, red, green, blue, alpha);
    }
}
