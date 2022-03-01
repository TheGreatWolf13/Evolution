package tgw.evolution.client.renderer.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemStack;
import tgw.evolution.client.models.entities.ModelSpear;
import tgw.evolution.items.ISpear;
import tgw.evolution.items.ItemJavelin;

public class RenderStackItemJavelin extends BlockEntityWithoutLevelRenderer {

    private final ModelSpear spear = new ModelSpear();

    public RenderStackItemJavelin(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
        super(dispatcher, modelSet);
    }

    @Override
    public void renderByItem(ItemStack stack,
                             ItemTransforms.TransformType transformType,
                             PoseStack matrices,
                             MultiBufferSource buffer,
                             int packedLight,
                             int packedOverlay) {
        if (stack.getItem() instanceof ItemJavelin) {
            matrices.pushPose();
            matrices.scale(1.0F, -1.0F, -1.0F);
            matrices.translate(0, -0.1, 0);
            matrices.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
            VertexConsumer modelBuffer = ItemRenderer.getFoilBuffer(buffer,
                                                                    this.spear.renderType(((ISpear) stack.getItem()).getTexture()),
                                                                    false,
                                                                    stack.hasFoil());
            this.spear.renderToBuffer(matrices, modelBuffer, packedLight, packedOverlay, 1.0f, 1.0f, 1.0f, 1.0f);
            matrices.popPose();
        }
    }
}
