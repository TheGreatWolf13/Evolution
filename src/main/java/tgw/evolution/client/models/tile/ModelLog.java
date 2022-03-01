package tgw.evolution.client.models.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.RenderType;

public class ModelLog extends Model {

//    private final ModelRenderer log;

    public ModelLog(int x, int y) {
        super(RenderType::entitySolid);
//        this.texWidth = 40;
//        this.texHeight = 8;
//        this.log = new ModelRenderer(this, 0, 0);
//        this.log.addBox(-16, 8 + 4 * y, 4 * x, 16, 4, 4);
//        MathHelper.setRotationAngle(this.log, 0, MathHelper.PI_OVER_2, 0);
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
//        this.log.render(matrices, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
