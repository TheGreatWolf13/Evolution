package tgw.evolution.client.renderer.entities;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class RenderDummy<E extends Entity> extends EntityRenderer<E> {

    public RenderDummy(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(E entity) {
        return TextureManager.INTENTIONAL_MISSING_TEXTURE;
    }

    @Override
    public void render(E entity, float yaw, float partialTicks, PoseStack matrices, MultiBufferSource buffer, int packedLight) {
    }
}
