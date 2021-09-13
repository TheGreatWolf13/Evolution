package tgw.evolution.client.renderer.item;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Vector3f;
import tgw.evolution.client.models.entities.ModelSpear;
import tgw.evolution.items.ISpear;
import tgw.evolution.items.ItemJavelin;

public class RenderStackItemJavelin extends ItemStackTileEntityRenderer {

    private final ModelSpear spear = new ModelSpear();

    @Override
    public void renderByItem(ItemStack stack,
                             ItemCameraTransforms.TransformType transformType,
                             MatrixStack matrices,
                             IRenderTypeBuffer buffer,
                             int packedLight,
                             int packedOverlay) {
        if (stack.getItem() instanceof ItemJavelin) {
            matrices.pushPose();
            matrices.scale(1.0F, -1.0F, -1.0F);
            matrices.translate(0, -0.1, 0);
            matrices.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
            IVertexBuilder modelBuffer = ItemRenderer.getFoilBuffer(buffer,
                                                                    this.spear.renderType(((ISpear) stack.getItem()).getTexture()),
                                                                    false,
                                                                    stack.hasFoil());
            this.spear.renderToBuffer(matrices, modelBuffer, packedLight, packedOverlay, 1.0f, 1.0f, 1.0f, 1.0f);
            matrices.popPose();
        }
    }
}
