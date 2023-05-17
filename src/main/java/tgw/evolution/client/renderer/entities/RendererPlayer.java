package tgw.evolution.client.renderer.entities;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;
import tgw.evolution.client.layers.LayerItemInHandPlayer;
import tgw.evolution.client.models.entities.ModelPlayer;
import tgw.evolution.client.renderer.ClientRenderer;
import tgw.evolution.events.ClientEvents;
import tgw.evolution.patches.IPoseStackPatch;
import tgw.evolution.util.ArmPose;
import tgw.evolution.util.hitbox.hms.HMPlayer;
import tgw.evolution.util.hitbox.hrs.HRPlayer;
import tgw.evolution.util.math.MathHelper;

public class RendererPlayer extends LivingEntityRenderer<AbstractClientPlayer, ModelPlayer<AbstractClientPlayer>>
        implements HRPlayer<AbstractClientPlayer> {

    public RendererPlayer(EntityRendererProvider.Context context, boolean slim) {
        super(context, new ModelPlayer<>(context.bakeLayer(slim ? ModelLayers.PLAYER_SLIM : ModelLayers.PLAYER), slim), 0.5F);
//        this.addLayer(new HumanoidArmorLayer<>(this, new HumanoidModel(
//                context.bakeLayer(slim ? ModelLayers.PLAYER_SLIM_INNER_ARMOR : ModelLayers.PLAYER_INNER_ARMOR)), new HumanoidModel(
//                context.bakeLayer(slim ? ModelLayers.PLAYER_SLIM_OUTER_ARMOR : ModelLayers.PLAYER_OUTER_ARMOR))));
        this.addLayer(new LayerItemInHandPlayer<>(this));
//        this.addLayer(new ArrowLayer<>(context, this));
//        this.addLayer(new Deadmau5EarsLayer(this));
//        this.addLayer(new CapeLayer(this));
//        this.addLayer(new CustomHeadLayer<>(this, context.getModelSet()));
//        this.addLayer(new ElytraLayer<>(this, context.getModelSet()));
//        this.addLayer(new ParrotOnShoulderLayer<>(this, context.getModelSet()));
//        this.addLayer(new SpinAttackEffectLayer<>(this, context.getModelSet()));
//        this.addLayer(new BeeStingerLayer<>(this));
//        this.addLayer(new LayerBelt(this));
//        this.addLayer(new LayerBack(this));
    }

    private static ArmPose getArmPose(AbstractClientPlayer player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.isEmpty()) {
            return ArmPose.EMPTY;
        }
        if (player.getUsedItemHand() == hand && player.getUseItemRemainingTicks() > 0) {
            UseAnim anim = stack.getUseAnimation();
            if (anim == UseAnim.BLOCK) {
                return ArmPose.BLOCK;
            }
            if (anim == UseAnim.BOW) {
                return ArmPose.BOW_AND_ARROW;
            }
            if (anim == UseAnim.SPEAR) {
                return ArmPose.THROW_SPEAR;
            }
            if (anim == UseAnim.CROSSBOW && hand == player.getUsedItemHand()) {
                return ArmPose.CROSSBOW_CHARGE;
            }
            if (anim == UseAnim.SPYGLASS) {
                return ArmPose.SPYGLASS;
            }
        }
        else if (!player.swinging && stack.is(Items.CROSSBOW) && CrossbowItem.isCharged(stack)) {
            return ArmPose.CROSSBOW_HOLD;
        }
        return ArmPose.ITEM;
    }

    @Override
    public Vec3 getRenderOffset(AbstractClientPlayer entity, float partialTicks) {
        return this.renderOffset(entity, partialTicks);
    }

    /**
     * Returns the location of an entity's texture.
     */
    @Override
    public ResourceLocation getTextureLocation(AbstractClientPlayer pEntity) {
        return pEntity.getSkinTextureLocation();
    }

    @Override
    public HMPlayer<AbstractClientPlayer> model() {
        return this.model;
    }

    @Override
    public void render(AbstractClientPlayer player,
                       float entityYaw,
                       float partialTicks,
                       PoseStack matrices,
                       MultiBufferSource buffer,
                       int light) {
        this.modelProperties(player);
        //When rendering the player in first person, hide certain parts of the player model to not clip into the camera in certain situations
        ClientRenderer renderer = ClientEvents.getInstance().getRenderer();
        if (renderer.isRenderingPlayer()) {
            renderer.setVisibility(HumanoidArm.LEFT, true);
            renderer.setVisibility(HumanoidArm.RIGHT, true);
            //The hat should never be visible because it's placed enclosing the head, where the camera is placed
            this.model.hat.visible = false;
            //If in the swimming state, the head should not be visible, as the camera and the head will rotate in different ways
            if (player.getSwimAmount(partialTicks) > 0.0f) {
                this.model.head.visible = false;
            }
            if (player.isUsingItem() && player.getUseItem().getUseAnimation() == UseAnim.SPYGLASS) {
                HumanoidArm arm = player.getMainArm();
                if (player.getUsedItemHand() != InteractionHand.MAIN_HAND) {
                    arm = arm.getOpposite();
                }
                renderer.setVisibility(arm, false);
                switch (arm) {
                    case RIGHT -> {
                        this.model.armR.visible = false;
                        this.model.clothesArmR.visible = false;
                    }
                    case LEFT -> {
                        this.model.armL.visible = false;
                        this.model.clothesArmL.visible = false;
                    }
                }
            }
        }
        super.render(player, entityYaw, partialTicks, matrices, buffer, light);
    }

    @Override
    protected void renderNameTag(AbstractClientPlayer entity, Component name, PoseStack matrices, MultiBufferSource buffer, int light) {
        double dist = this.entityRenderDispatcher.distanceToSqr(entity);
        matrices.pushPose();
        if (dist < 100) {
            Scoreboard scoreboard = entity.getScoreboard();
            Objective objective = scoreboard.getDisplayObjective(2);
            if (objective != null) {
                Score score = scoreboard.getOrCreatePlayerScore(entity.getScoreboardName(), objective);
                super.renderNameTag(entity, new TextComponent(Integer.toString(score.getScore())).append(" ").append(objective.getDisplayName()),
                                    matrices, buffer, light);
                matrices.translate(0, 9.0F * 1.15F * 0.025F, 0);
            }
        }
        super.renderNameTag(entity, name, matrices, buffer, light);
        matrices.popPose();
    }

    @Override
    protected void scale(AbstractClientPlayer entity, PoseStack matrices, float partialTicks) {
        matrices.scale(0.937_5F, 0.937_5F, 0.937_5F);
    }

    private void setModelProperties(AbstractClientPlayer player) {
        ModelPlayer<AbstractClientPlayer> model = this.getModel();
        if (player.isSpectator()) {
            model.setAllVisible(false);
            model.head.visible = true;
            model.hat.visible = true;
        }
        else {
            model.setAllVisible(true);
            model.hat.visible = player.isModelPartShown(PlayerModelPart.HAT);
            model.clothesBody.visible = player.isModelPartShown(PlayerModelPart.JACKET);
            model.clothesLegL.visible = player.isModelPartShown(PlayerModelPart.LEFT_PANTS_LEG);
            model.clothesLegR.visible = player.isModelPartShown(PlayerModelPart.RIGHT_PANTS_LEG);
            model.clothesArmL.visible = player.isModelPartShown(PlayerModelPart.LEFT_SLEEVE);
            model.clothesArmR.visible = player.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE);
            model.crouching = player.isCrouching();
            ArmPose mainArmPose = getArmPose(player, InteractionHand.MAIN_HAND);
            ArmPose offArmPose = getArmPose(player, InteractionHand.OFF_HAND);
            if (mainArmPose.isTwoHanded()) {
                offArmPose = player.getOffhandItem().isEmpty() ? ArmPose.EMPTY : ArmPose.ITEM;
            }
            if (player.getMainArm() == HumanoidArm.RIGHT) {
                model.rightArmPose = mainArmPose;
                model.leftArmPose = offArmPose;
            }
            else {
                model.rightArmPose = offArmPose;
                model.leftArmPose = mainArmPose;
            }
        }
    }

    @Override
    public void setShadowRadius(float radius) {
        this.shadowRadius = radius;
    }

    @Override
    protected void setupRotations(AbstractClientPlayer player, PoseStack matrices, float ageInTicks, float rotationYaw, float partialTicks) {
        float swimAmount = player.getSwimAmount(partialTicks);
        IPoseStackPatch matricesExt = MathHelper.getExtendedMatrix(matrices);
        if (player.isFallFlying()) {
            super.setupRotations(player, matrices, ageInTicks, rotationYaw, partialTicks);
            float f1 = player.getFallFlyingTicks() + partialTicks;
            float f2 = Mth.clamp(f1 * f1 / 100.0F, 0.0F, 1.0F);
            if (!player.isAutoSpinAttack()) {
                matricesExt.mulPoseX(f2 * (-90.0F - player.getXRot()));
            }
            Vec3 viewVec = player.getViewVector(partialTicks);
            Vec3 motion = player.getDeltaMovement();
            double horizMotionSqr = motion.horizontalDistanceSqr();
            double horizViewSqr = viewVec.horizontalDistanceSqr();
            if (horizMotionSqr > 0 && horizViewSqr > 0) {
                double d2 = (motion.x * viewVec.x + motion.z * viewVec.z) * Mth.fastInvSqrt(horizMotionSqr * horizViewSqr);
                double d3 = motion.x * viewVec.z - motion.z * viewVec.x;
                matricesExt.mulPoseYRad((float) (Math.signum(d3) * Math.acos(d2)));
            }
        }
        else if (swimAmount > 0.0F) {
            super.setupRotations(player, matrices, ageInTicks, rotationYaw, partialTicks);
            float desiredXRot = player.isInWater() ? -90.0F - player.getXRot() : -90.0F;
            float interpXRot = Mth.lerp(swimAmount, 0.0F, desiredXRot);
            if (player.isVisuallySwimming()) {
                if (!player.isInWater()) {
                    //Crawling pose
                    matricesExt.mulPoseX(interpXRot);
                    matrices.translate(0, -1, 0.3);
                }
                else {
                    //Swimming pose
                    matrices.translate(0, 0.4, 0);
                    matricesExt.mulPoseX(interpXRot);
                    matrices.translate(0, -1.4, -0.25);
                }
            }
            else {
                matricesExt.mulPoseX(interpXRot);
                matrices.translate(0, -1.3, 0);
            }
        }
        else {
            super.setupRotations(player, matrices, ageInTicks, rotationYaw, partialTicks);
        }
    }
}
