package tgw.evolution.client.models.entities;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.RenderType;

public class ModelTorch extends Model {

//    private final ModelRenderer model;

    public ModelTorch() {
        super(RenderType::entitySolid);
//        this.texWidth = 16;
//        this.texHeight = 16;
//        this.model = new ModelRenderer(this, 0, 0);
//        this.model.addBox("", -1.0f, 0.0F, -1.0f, 2, 10, 2, 0.0F, 4, 4);
    }

    @Override
    public void renderToBuffer(PoseStack matrices,
                               VertexConsumer vertex,
                               int packetLight,
                               int packetOverlay,
                               float red,
                               float green,
                               float blue,
                               float alpha) {
//        this.model.render(matrices, vertex, packetLight, packetOverlay, red, green, blue, alpha);
    }
}
