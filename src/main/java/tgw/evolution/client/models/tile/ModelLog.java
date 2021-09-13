package tgw.evolution.client.models.tile;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;
import tgw.evolution.util.MathHelper;

public class ModelLog extends Model {

    private final ModelRenderer log;

    public ModelLog(int x, int y) {
        super(RenderType::entitySolid);
        this.texWidth = 40;
        this.texHeight = 8;
        this.log = new ModelRenderer(this, 0, 0);
        this.log.addBox(-16, 8 + 4 * y, 4 * x, 16, 4, 4);
        MathHelper.setRotationAngle(this.log, 0, MathHelper.PI_OVER_2, 0);
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
        this.log.render(matrices, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
