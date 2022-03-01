package tgw.evolution.client.models.entities;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.RenderType;

public class ModelSpear extends Model {

//    private final ModelRenderer headMain;
//    private final ModelRenderer headSideEast;
//    private final ModelRenderer headSideWest;
//    private final ModelRenderer pole;

    public ModelSpear() {
        super(RenderType::entitySolid);
//        this.texWidth = 32;
//        this.texHeight = 32;
//        this.pole = new ModelRenderer(this, 0, 0);
//        this.pole.addBox(-0.5f, -27.0f, -0.5f, 1, 31, 1, 0.0F);
//        this.headMain = new ModelRenderer(this, 4, 0);
//        this.headMain.addBox(-1.5f, -4.0f, -0.5f, 3, 7, 1);
//        this.headSideWest = new ModelRenderer(this, 8, 8);
//        this.headSideWest.addBox(1.5F, -3.0f, -0.5f, 1, 4, 1);
//        this.headSideEast = new ModelRenderer(this, 4, 8);
//        this.headSideEast.addBox(-2.5f, -3.0f, -0.5f, 1, 4, 1);
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
//        this.pole.render(matrices, vertex, packetLight, packetOverlay, red, green, blue, alpha);
//        this.headMain.render(matrices, vertex, packetLight, packetOverlay, red, green, blue, alpha);
//        this.headSideWest.render(matrices, vertex, packetLight, packetOverlay, red, green, blue, alpha);
//        this.headSideEast.render(matrices, vertex, packetLight, packetOverlay, red, green, blue, alpha);
    }
}
