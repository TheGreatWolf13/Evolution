package tgw.evolution.client.renderer.entities;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import tgw.evolution.Evolution;
import tgw.evolution.client.models.entities.ModelCow;
import tgw.evolution.entities.EntityCow;
import tgw.evolution.util.MathHelper;

@OnlyIn(Dist.CLIENT)
public class RenderCow extends MobRenderer<EntityCow, ModelCow> {

    private static final ModelCow MODEL = new ModelCow();
    private static final ResourceLocation DEFAULT = Evolution.location("textures/entity/cattle/cow.png");
    private static final ResourceLocation SLEEPING = Evolution.location("textures/entity/cattle/cow_sleeping.png");
    private static final ResourceLocation DEAD = Evolution.location("textures/entity/cattle/cow_dead.png");
    private static final ResourceLocation SKELETON = Evolution.location("textures/entity/cattle/cow_skeleton.png");

    public RenderCow(EntityRendererManager manager) {
        super(manager, MODEL, 0.7F);
    }

    @Override
    protected void applyRotations(EntityCow entityLiving, float ageInTicks, float rotationYaw, float partialTicks) {
        GlStateManager.rotatef(180.0F - rotationYaw, 0.0F, 1.0F, 0.0F);
        if (entityLiving.isDead()) {
            float f = (MathHelper.clampMax(entityLiving.getDeathTime(), 20) + partialTicks - 1.0F) / 20.0F * 1.6F;
            f = MathHelper.sqrt(f);
            if (f > 1.0F) {
                f = 1.0F;
            }
            GlStateManager.rotatef(f * this.getDeathMaxRotation(entityLiving), 0.0F, 0.0F, 1.0F);
            GlStateManager.translatef(0.32F, -0.75f, 0.0F);
        }
        else if (entityLiving.isSleeping()) {
            GlStateManager.translatef(0.0F, -0.7f, 0.0F);
        }
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityCow entity) {
        if (entity.isDead()) {
            if (entity.isSkeleton()) {
                return SKELETON;
            }
            return DEAD;
        }
        if (entity.isSleeping()) {
            return SLEEPING;
        }
        return DEFAULT;
    }
}