//package tgw.evolution.client.renderer.entities;
//
//import com.mojang.blaze3d.platform.GlStateManager;
//import net.minecraft.client.renderer.entity.EntityRendererManager;
//import net.minecraft.client.renderer.entity.MobRenderer;
//import net.minecraft.entity.Pose;
//import net.minecraft.util.Direction;
//import net.minecraft.util.ResourceLocation;
//import net.minecraft.util.math.MathHelper;
//import net.minecraft.util.text.TextFormatting;
//import net.minecraftforge.api.distmarker.Dist;
//import net.minecraftforge.api.distmarker.OnlyIn;
//import tgw.evolution.Evolution;
//import tgw.evolution.client.models.entities.ModelBull;
//import tgw.evolution.entities.EntityBull;
//
//@OnlyIn(Dist.CLIENT)
//public class RenderBull extends MobRenderer<EntityBull, ModelBull> {
//
//    public static final ResourceLocation DEFAULT = Evolution.location("textures/entity/cattle/cow.png");
//    private static final ModelBull MODEL = new ModelBull();
//    private static final ResourceLocation DEAD = Evolution.location("textures/entity/cattle/cow_dead.png");
//    private static final ResourceLocation SKELETON = Evolution.location("textures/entity/cattle/cow_skeleton.png");
//
//    public RenderBull(EntityRendererManager manager) {
//        super(manager, MODEL, 0.7F);
//    }
//
//    private static float bedAngle(Direction direction) {
//        switch (direction) {
//            case SOUTH:
//                return 90.0F;
//            case NORTH:
//                return 270.0F;
//            case EAST:
//                return 180.0F;
//            default:
//                return 0.0F;
//        }
//    }
//
//    @Override
//    protected ResourceLocation getEntityTexture(EntityBull entity) {
//        if (entity.isDead()) {
//            if (entity.isSkeleton()) {
//                return SKELETON;
//            }
//            return DEAD;
//        }
//        return DEFAULT;
//    }
//
//    @Override
//    protected void applyRotations(EntityBull entityLiving, float ageInTicks, float rotationYaw, float partialTicks) {
//        Pose pose = entityLiving.getPose();
//        if (pose != Pose.SLEEPING) {
//            GlStateManager.rotatef(180.0F - rotationYaw, 0.0F, 1.0F, 0.0F);
//        }
//        if (entityLiving.getDeathTime() > 0) {
//            float f = (tgw.evolution.util.MathHelper.clampMax(entityLiving.getDeathTime(), 20) + partialTicks - 1.0F) / 20.0F * 1.6F;
//            f = MathHelper.sqrt(f);
//            if (f > 1.0F) {
//                f = 1.0F;
//            }
//            GlStateManager.rotatef(f * this.getDeathMaxRotation(entityLiving), 0F, 0F, 1F);
//            GlStateManager.translatef(0.32F, -0.75F, 0F);
//        }
//        else if (entityLiving.isSpinAttacking()) {
//            GlStateManager.rotatef(-90.0F - entityLiving.rotationPitch, 1.0F, 0.0F, 0.0F);
//            GlStateManager.rotatef((entityLiving.ticksExisted + partialTicks) * -75.0F, 0.0F, 1.0F, 0.0F);
//        }
//        else if (pose == Pose.SLEEPING) {
//            Direction direction = entityLiving.getBedDirection();
//            GlStateManager.rotatef(direction != null ? bedAngle(direction) : rotationYaw, 0.0F, 1.0F, 0.0F);
//            GlStateManager.rotatef(this.getDeathMaxRotation(entityLiving), 0.0F, 0.0F, 1.0F);
//            GlStateManager.rotatef(270.0F, 0.0F, 1.0F, 0.0F);
//        }
//        else if (entityLiving.hasCustomName()) {
//            String s = TextFormatting.getTextWithoutFormattingCodes(entityLiving.getName().getString());
//            if ("Dinnerbone".equals(s) || "Grumm".equals(s)) {
//                GlStateManager.translatef(0.0F, entityLiving.getHeight() + 0.1F, 0.0F);
//                GlStateManager.rotatef(180.0F, 0.0F, 0.0F, 1.0F);
//            }
//        }
//    }
//}