package tgw.evolution.client.renderer.entities;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import tgw.evolution.entities.projectiles.EntitySpear;

public class RenderSpear extends EntityRenderer<EntitySpear> {

    public RenderSpear(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(EntitySpear spear) {
        return TextureAtlas.LOCATION_BLOCKS;
    }

    @Override
    public void render(EntitySpear entity, float yaw, float partialTicks, PoseStack matrices, MultiBufferSource buffer, int packedLight) {
        matrices.pushPose();
        matrices.mulPoseY(Mth.lerp(partialTicks, entity.yRotO, entity.getYRot()) - 90.0F);
        matrices.mulPoseZ(Mth.lerp(partialTicks, entity.xRotO, entity.getXRot()) - 90.0F);
        matrices.translate(-0.05, -0.7, 0);
        Minecraft.getInstance()
                 .getItemRenderer()
                 .renderStatic(entity.getStack(), ItemTransforms.TransformType.HEAD, packedLight, OverlayTexture.NO_OVERLAY, matrices, buffer, 0);
        matrices.popPose();
        super.render(entity, yaw, partialTicks, matrices, buffer, packedLight);
    }
}
