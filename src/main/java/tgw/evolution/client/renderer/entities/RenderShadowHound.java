//package tgw.evolution.client.renderer.entities;
//
//import com.mojang.blaze3d.platform.GlStateManager;
//import net.minecraft.client.renderer.entity.EntityRendererManager;
//import net.minecraft.client.renderer.entity.MobRenderer;
//import net.minecraft.util.ResourceLocation;
//import tgw.evolution.Evolution;
//import tgw.evolution.client.models.entities.ModelShadowHound;
//import tgw.evolution.entities.EntityShadowHound;
//import tgw.evolution.util.math.MathHelper;
//
//public class RenderShadowHound extends MobRenderer<EntityShadowHound, ModelShadowHound> {
//
//    public static final ResourceLocation DEFAULT = Evolution.location("textures/entity/shadowhound.png");
//    private static final ModelShadowHound MODEL = new ModelShadowHound();
//
//    public RenderShadowHound(EntityRendererManager manager) {
//        super(manager, MODEL, 0.5F);
//    }
//
//    @Override
//    protected ResourceLocation getEntityTexture(EntityShadowHound entity) {
//        return DEFAULT;
//    }
//
//    @Override
//    protected void applyRotations(EntityShadowHound entityLiving, float ageInTicks, float rotationYaw, float partialTicks) {
//        GlStateManager.rotatef(180.0F - rotationYaw, 0.0F, 1.0F, 0.0F);
//        if (entityLiving.isDead()) {
//            float f = (MathHelper.clampMax(entityLiving.getDeathTime(), 20) + partialTicks - 1.0F) / 20.0F * 1.6F;
//            f = MathHelper.sqrt(f);
//            if (f > 1.0F) {
//                f = 1.0F;
//            }
//            GlStateManager.rotatef(f * this.getDeathMaxRotation(entityLiving), 0F, 0F, 1F);
//            GlStateManager.translatef(0.32F, 0F, 0F);
//        }
//    }
//}
