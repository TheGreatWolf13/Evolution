package tgw.evolution.client.renderer.entities;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import tgw.evolution.Evolution;
import tgw.evolution.client.models.entities.ModelTorch;
import tgw.evolution.entities.projectiles.EntityTorch;

import javax.annotation.Nullable;

public class RenderTorch extends EntityRenderer<EntityTorch> {

    public static final ResourceLocation TORCH = Evolution.getResource("textures/entity/torch.png");
    private final ModelTorch model = new ModelTorch();

    public RenderTorch(EntityRendererManager renderManager) {
        super(renderManager);
    }

    @Nullable
    @Override
    public ResourceLocation getTextureLocation(EntityTorch entity) {
        return TORCH;
    }

    @Override
    public void render(EntityTorch entity, float yaw, float partialTicks, MatrixStack matrices, IRenderTypeBuffer buffer, int packedLight) {
        matrices.pushPose();
        matrices.mulPose(Vector3f.YP.rotationDegrees(MathHelper.lerp(partialTicks, entity.yRotO, entity.yRot) - 90.0F));
        matrices.mulPose(Vector3f.ZP.rotationDegrees(MathHelper.lerp(partialTicks, entity.xRotO, entity.xRot) + 90.0F));
        IVertexBuilder modelBuffer = ItemRenderer.getFoilBuffer(buffer, this.model.renderType(this.getTextureLocation(entity)), false, false);
        this.model.renderToBuffer(matrices, modelBuffer, packedLight, OverlayTexture.NO_OVERLAY, 1.0f, 1.0f, 1.0f, 1.0f);
        matrices.popPose();
        super.render(entity, yaw, partialTicks, matrices, buffer, packedLight);
    }
}
