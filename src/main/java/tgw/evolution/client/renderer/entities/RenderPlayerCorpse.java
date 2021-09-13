package tgw.evolution.client.renderer.entities;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.SkeletonRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;
import tgw.evolution.entities.EntityPlayerDummy;
import tgw.evolution.entities.EntitySkeletonDummy;
import tgw.evolution.entities.misc.EntityPlayerCorpse;

public class RenderPlayerCorpse extends EntityRenderer<EntityPlayerCorpse> {

    private final RenderPlayerDummy playerRendererAlex;
    private final RenderPlayerDummy playerRendererSteve;
    private final SkeletonRenderer skeletonRenderer;

    public RenderPlayerCorpse(EntityRendererManager renderManager) {
        super(renderManager);
        this.playerRendererSteve = new RenderPlayerDummy(renderManager, false);
        this.playerRendererAlex = new RenderPlayerDummy(renderManager, true);
        this.skeletonRenderer = new SkeletonRenderer(renderManager);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityPlayerCorpse entity) {
        return TextureManager.INTENTIONAL_MISSING_TEXTURE;
    }

    @Override
    public void render(EntityPlayerCorpse entity, float yaw, float partialTicks, MatrixStack matrices, IRenderTypeBuffer buffer, int packedLight) {
        super.render(entity, yaw, partialTicks, matrices, buffer, packedLight);
        matrices.pushPose();
        matrices.mulPose(Vector3f.YP.rotationDegrees(-entity.yRot));
        matrices.mulPose(Vector3f.XP.rotationDegrees(-90.0F));
        matrices.translate(0, -1, 3 / 16.0);
        if (entity.isSkeleton()) {
            EntitySkeletonDummy skeleton = entity.getSkeleton();
            this.skeletonRenderer.render(skeleton, yaw, 1.0F, matrices, buffer, packedLight);
        }
        else {
            EntityPlayerDummy player = entity.getPlayer();
            if ("default".equals(DefaultPlayerSkin.getSkinModelName(entity.getPlayerUUID()))) {
                this.playerRendererSteve.render(player, 0.0F, 1.0F, matrices, buffer, packedLight);
            }
            else {
                this.playerRendererAlex.render(player, 0.0F, 1.0F, matrices, buffer, packedLight);
            }
        }
        matrices.popPose();
    }
}
