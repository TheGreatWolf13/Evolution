package tgw.evolution.client.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import tgw.evolution.EvolutionClient;

public class LayerItemInHandPlayer<P extends Player, M extends EntityModel<P> & ArmedModel & HeadedModel> extends LayerItemInHand<P, M> {

    public LayerItemInHandPlayer(RenderLayerParent<P, M> renderer) {
        super(renderer);
    }

    @Override
    protected void renderArmWithItem(P player, ItemStack stack, ItemTransforms.TransformType type, HumanoidArm arm, PoseStack matrices, MultiBufferSource buffer, int light) {
        if (!EvolutionClient.getRenderer().shouldRenderArm(arm)) {
            return;
        }
        if (stack.is(Items.SPYGLASS) && player.getUseItem() == stack && player.swingTime == 0) {
            this.renderArmWithSpyglass(player, stack, arm, matrices, buffer, light);
        }
        else {
            super.renderArmWithItem(player, stack, type, arm, matrices, buffer, light);
        }
    }

    private void renderArmWithSpyglass(P entity, ItemStack stack, HumanoidArm arm, PoseStack matrices, MultiBufferSource buffer, int light) {
        matrices.pushPose();
        ModelPart head = this.getParentModel().getHead();
        float oldXRot = head.xRot;
        head.xRot = Mth.clamp(head.xRot, -Mth.HALF_PI, Mth.HALF_PI);
        head.translateAndRotate(matrices);
        head.xRot = oldXRot;
        CustomHeadLayer.translateToHead(matrices, false);
        boolean flag = arm == HumanoidArm.LEFT;
        matrices.translate((flag ? -2.5F : 2.5F) / 16.0, -0.062_5, 0.0);
        Minecraft.getInstance().getItemInHandRenderer().renderItem(entity, stack, ItemTransforms.TransformType.HEAD, false, matrices, buffer, light);
        matrices.popPose();
    }
}
