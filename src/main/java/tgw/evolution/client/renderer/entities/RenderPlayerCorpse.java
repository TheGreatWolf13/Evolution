package tgw.evolution.client.renderer.entities;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import tgw.evolution.Evolution;
import tgw.evolution.client.models.entities.ModelPlayerCorpse;
import tgw.evolution.entities.misc.EntityPlayerCorpse;

import javax.annotation.Nullable;
import java.util.Map;

public class RenderPlayerCorpse extends EntityRenderer<EntityPlayerCorpse> {

    private static final ModelPlayerCorpse ALEX = new ModelPlayerCorpse(true);
    private static final ModelPlayerCorpse STEVE = new ModelPlayerCorpse(false);
    private static final ModelPlayerCorpse SKELETON = new ModelPlayerCorpse();
    private static final ResourceLocation SKELETON_TEXTURE = Evolution.getResource("textures/entity/skeleton.png");

    public RenderPlayerCorpse(EntityRendererManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(EntityPlayerCorpse entity, double x, double y, double z, float entityYaw, float partialTicks) {
        this.bindEntityTexture(entity);
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.pushMatrix();
        GlStateManager.disableLighting();
        GlStateManager.translatef((float) x, (float) y + 1.5f, (float) z);
        GlStateManager.rotatef(entity.rotationYaw, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotatef(entity.rotationPitch - 180.0F, 0.0F, 0.0F, 1.0F);
        if (entity.isSkeleton()) {
            SKELETON.render(0.062_5f);
        }
        else if ("default".equals(DefaultPlayerSkin.getSkinType(entity.getPlayerUUID()))) {
            STEVE.render(0.062_5f);
        }
        else {
            ALEX.render(0.062_5f);
        }
        GlStateManager.popMatrix();
        GlStateManager.enableLighting();
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(EntityPlayerCorpse entity) {
        if (entity.isSkeleton()) {
            return SKELETON_TEXTURE;
        }
        ResourceLocation texture = DefaultPlayerSkin.getDefaultSkin(entity.getPlayerUUID());
        GameProfile profile = entity.getPlayerProfile();
        if (profile != null) {
            Minecraft minecraft = Minecraft.getInstance();
            Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> map = minecraft.getSkinManager().loadSkinFromCache(profile);
            if (map.containsKey(MinecraftProfileTexture.Type.SKIN)) {
                return minecraft.getSkinManager().loadSkin(map.get(MinecraftProfileTexture.Type.SKIN), MinecraftProfileTexture.Type.SKIN);
            }
            return DefaultPlayerSkin.getDefaultSkin(PlayerEntity.getUUID(profile));
        }
        return texture;
    }
}
