package tgw.evolution.client.renderer.entities;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.SkeletonRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;
import tgw.evolution.entities.EntityPlayerDummy;
import tgw.evolution.entities.EntitySkeletonDummy;
import tgw.evolution.entities.misc.EntityPlayerCorpse;
import tgw.evolution.util.constants.CommonRotations;
import tgw.evolution.util.math.MathHelper;

public class RenderPlayerCorpse extends EntityRenderer<EntityPlayerCorpse> {

    private final RenderPlayerDummy playerRendererAlex;
    private final RenderPlayerDummy playerRendererSteve;
    private final SkeletonRenderer skeletonRenderer;

    public RenderPlayerCorpse(EntityRendererProvider.Context context) {
        super(context);
        this.playerRendererSteve = new RenderPlayerDummy(context, false);
        this.playerRendererAlex = new RenderPlayerDummy(context, true);
        this.skeletonRenderer = new SkeletonRenderer(context);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityPlayerCorpse entity) {
        return TextureManager.INTENTIONAL_MISSING_TEXTURE;
    }

    @Override
    public void render(EntityPlayerCorpse entity, float yaw, float partialTicks, PoseStack matrices, MultiBufferSource buffer, int packedLight) {
        super.render(entity, yaw, partialTicks, matrices, buffer, packedLight);
        matrices.pushPose();
        MathHelper.getExtendedMatrix(matrices).mulPoseY(-entity.getYRot());
        matrices.mulPose(CommonRotations.XN90);
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
