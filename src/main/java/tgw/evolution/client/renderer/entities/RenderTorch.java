package tgw.evolution.client.renderer.entities;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import tgw.evolution.Evolution;
import tgw.evolution.client.models.entities.ModelTorch;
import tgw.evolution.entities.projectiles.EntityTorch;

import javax.annotation.Nullable;

public class RenderTorch extends EntityRenderer<EntityTorch> {

    public static final ResourceLocation TORCH = Evolution.location("textures/entity/torch.png");
    private final ModelTorch model = new ModelTorch();

    public RenderTorch(EntityRendererManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(EntityTorch entity, double x, double y, double z, float entityYaw, float partialTicks) {
        this.bindEntityTexture(entity);
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.pushMatrix();
        GlStateManager.disableLighting();
        GlStateManager.translatef((float) x, (float) y, (float) z);
        GlStateManager.rotatef(MathHelper.lerp(partialTicks, entity.prevRotationYaw, entity.rotationYaw) - 90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotatef(MathHelper.lerp(partialTicks, entity.prevRotationPitch, entity.rotationPitch) + 90.0F, 0.0F, 0.0F, 1.0F);
        this.model.renderer();
        GlStateManager.popMatrix();
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
        GlStateManager.enableLighting();
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(EntityTorch entity) {
        return TORCH;
    }
}
