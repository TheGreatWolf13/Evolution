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
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ItemStack;
import tgw.evolution.events.ClientEvents;

public class LayerBack extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    public LayerBack(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> playerRenderer) {
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
        if (player.isModelPartShown(PlayerModelPart.CAPE) && player.getCloakTextureLocation() != null) {
            return;
        }
        ItemStack backStack = ClientEvents.BACK_ITEMS.getOrDefault(player.getId(), ItemStack.EMPTY);
        if (!backStack.isEmpty()) {
            int sideOffset = player.getMainArm() == HumanoidArm.RIGHT ? 1 : -1;
            matrices.pushPose();
            this.getParentModel().body.translateAndRotate(matrices);
            matrices.translate(-sideOffset * 0.1, 0.25, 0.17);
            matrices.scale(sideOffset * 0.75f, 0.75f, 0.75f);
            Minecraft.getInstance()
                     .getItemRenderer()
                     .renderStatic(player,
                                   backStack,
                                   ItemTransforms.TransformType.GUI,
                                   false,
                                   matrices,
                                   buffer,
                                   player.level,
                                   packetLight,
                                   OverlayTexture.NO_OVERLAY,
                                   0);
            matrices.popPose();
        }
    }
}
