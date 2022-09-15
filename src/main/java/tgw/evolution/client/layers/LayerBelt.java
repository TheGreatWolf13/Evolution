package tgw.evolution.client.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import tgw.evolution.events.ClientEvents;
import tgw.evolution.util.constants.CommonRotations;

@OnlyIn(Dist.CLIENT)
public class LayerBelt extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    public LayerBelt(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> playerRenderer) {
        super(playerRenderer);
    }

    @Override
    public void render(PoseStack matrices,
                       MultiBufferSource buffer,
                       int packetLight,
                       AbstractClientPlayer player,
                       float limbSwing,
                       float limbSwingAmount,
                       float partialTicks,
                       float ageInTicks,
                       float headYaw,
                       float headPitch) {
        ItemStack beltStack = ClientEvents.BELT_ITEMS.getOrDefault(player.getId(), ItemStack.EMPTY);
        if (!beltStack.isEmpty()) {
            int sideOffset = player.getMainArm() == HumanoidArm.RIGHT ? -1 : 1;
            matrices.pushPose();
            this.getParentModel().body.translateAndRotate(matrices);
            matrices.mulPose(CommonRotations.YP90);
            matrices.translate(-0.1, -0.85, sideOffset * 0.28);
            matrices.scale(0.75f, 0.75f, 0.75f);
            matrices.mulPose(CommonRotations.ZP180);
            Minecraft.getInstance()
                     .getItemRenderer()
                     .renderStatic(player, beltStack, ItemTransforms.TransformType.NONE, player.getMainArm() == HumanoidArm.LEFT, matrices, buffer,
                                   player.level, packetLight, OverlayTexture.NO_OVERLAY, 0);
            matrices.popPose();
        }
    }
}
