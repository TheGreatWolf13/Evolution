package tgw.evolution.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tgw.evolution.util.constants.CommonRotations;

@Mixin(ItemInHandLayer.class)
public abstract class ItemInHandLayerMixin<T extends LivingEntity, M extends EntityModel<T> & ArmedModel> extends RenderLayer<T, M> {

    public ItemInHandLayerMixin(RenderLayerParent<T, M> pRenderer) {
        super(pRenderer);
    }

    /**
     * @author TheGreatWolf
     * @reason Fix HMs
     */
    @Override
    @Overwrite
    public void render(PoseStack matrices,
                       MultiBufferSource buffer,
                       int light,
                       T entity,
                       float limbSwing,
                       float limbSwingAmount,
                       float partialTicks,
                       float ageInTicks,
                       float netHeadYaw,
                       float headPitch) {
        boolean rightHanded = entity.getMainArm() == HumanoidArm.RIGHT;
        ItemStack leftHandStack = rightHanded ? entity.getOffhandItem() : entity.getMainHandItem();
        ItemStack rightHandStack = rightHanded ? entity.getMainHandItem() : entity.getOffhandItem();
        if (!leftHandStack.isEmpty() || !rightHandStack.isEmpty()) {
            matrices.pushPose();
            if (this.getParentModel().young) {
                matrices.translate(0.0, -0.75, 0.0);
                matrices.scale(0.5F, 0.5F, 0.5F);
            }
            this.renderArmWithItem(entity, rightHandStack, ItemTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, HumanoidArm.RIGHT, matrices,
                                   buffer, light);
            this.renderArmWithItem(entity, leftHandStack, ItemTransforms.TransformType.THIRD_PERSON_LEFT_HAND, HumanoidArm.LEFT, matrices,
                                   buffer, light);
            matrices.popPose();
        }
    }

    /**
     * @author TheGreatWolf
     * @reason Fix HMs
     */
    @Overwrite
    protected void renderArmWithItem(LivingEntity entity,
                                     ItemStack stack,
                                     ItemTransforms.TransformType type,
                                     HumanoidArm arm,
                                     PoseStack matrices,
                                     MultiBufferSource buffer,
                                     int light) {
        if (!stack.isEmpty()) {
            matrices.pushPose();
            this.getParentModel().translateToHand(arm, matrices);
            matrices.mulPose(CommonRotations.XN90);
            boolean leftArm = arm == HumanoidArm.LEFT;
            matrices.translate((leftArm ? -1 : 1) / 16.0, 2 / 16.0, -6 / 16.0);
            Minecraft.getInstance().getItemInHandRenderer().renderItem(entity, stack, type, leftArm, matrices, buffer, light);
            matrices.popPose();
        }
    }
}
