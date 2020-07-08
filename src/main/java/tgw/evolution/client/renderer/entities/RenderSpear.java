package tgw.evolution.client.renderer.entities;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import tgw.evolution.client.models.entities.ModelSpear;
import tgw.evolution.entities.projectiles.EntitySpear;

public class RenderSpear extends EntityRenderer<EntitySpear> {

    private final ModelSpear model = new ModelSpear();

    public RenderSpear(EntityRendererManager manager) {
        super(manager);
    }

    @Override
    public void doRender(EntitySpear entity, double x, double y, double z, float entityYaw, float partialTicks) {
        this.bindEntityTexture(entity);
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.pushMatrix();
        GlStateManager.disableLighting();
        GlStateManager.translatef((float) x, (float) y, (float) z);
        GlStateManager.rotatef(MathHelper.lerp(partialTicks, entity.prevRotationYaw, entity.rotationYaw) - 90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotatef(MathHelper.lerp(partialTicks, entity.prevRotationPitch, entity.rotationPitch) - 90.0F, 0.0F, 0.0F, 1.0F);
        this.model.render();
        GlStateManager.popMatrix();
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
        GlStateManager.enableLighting();
    }

    @Override
    protected ResourceLocation getEntityTexture(EntitySpear spear) {
        return spear.getTextureName();
    }
}
