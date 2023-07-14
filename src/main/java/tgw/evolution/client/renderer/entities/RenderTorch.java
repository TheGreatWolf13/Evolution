package tgw.evolution.client.renderer.entities;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import tgw.evolution.Evolution;
import tgw.evolution.client.models.entities.ModelTorch;
import tgw.evolution.entities.projectiles.EntityTorch;

public class RenderTorch extends EntityRenderer<EntityTorch> {

    public static final ResourceLocation TORCH = Evolution.getResource("textures/entity/torch.png");
    private final ModelTorch model = new ModelTorch();

    public RenderTorch(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityTorch entity) {
        return TORCH;
    }

    @Override
    public void render(EntityTorch entity, float yaw, float partialTicks, PoseStack matrices, MultiBufferSource buffer, int packedLight) {
        matrices.pushPose();
        matrices.mulPose(Vector3f.YP.rotationDegrees(Mth.lerp(partialTicks, entity.yRotO, entity.getYRot()) - 90.0F));
        matrices.mulPose(Vector3f.ZP.rotationDegrees(Mth.lerp(partialTicks, entity.xRotO, entity.getXRot()) + 90.0F));
        VertexConsumer modelBuffer = ItemRenderer.getFoilBuffer(buffer, this.model.renderType(this.getTextureLocation(entity)), false, false);
        this.model.renderToBuffer(matrices, modelBuffer, packedLight, OverlayTexture.NO_OVERLAY, 1.0f, 1.0f, 1.0f, 1.0f);
        matrices.popPose();
        super.render(entity, yaw, partialTicks, matrices, buffer, packedLight);
    }
}
