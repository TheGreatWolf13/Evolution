package tgw.evolution.client.layers;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.item.ItemStack;
import net.minecraft.util.HandSide;
import tgw.evolution.events.ClientEvents;

public class LayerBack extends LayerRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>> {

    public LayerBack(IEntityRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>> playerRenderer) {
        super(playerRenderer);
    }

    @Override
    public void render(MatrixStack matrices,
                       IRenderTypeBuffer buffer,
                       int packetLight,
                       AbstractClientPlayerEntity player,
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
            int sideOffset = player.getMainArm() == HandSide.RIGHT ? 1 : -1;
            matrices.pushPose();
            this.getParentModel().body.translateAndRotate(matrices);
            matrices.translate(-sideOffset * 0.1, 0.25, 0.17);
            matrices.scale(sideOffset * 0.75f, 0.75f, 0.75f);
            Minecraft.getInstance()
                     .getItemRenderer()
                     .renderStatic(player,
                                   backStack,
                                   ItemCameraTransforms.TransformType.GUI,
                                   false,
                                   matrices,
                                   buffer,
                                   player.level,
                                   packetLight,
                                   OverlayTexture.NO_OVERLAY);
            matrices.popPose();
        }
    }
}
