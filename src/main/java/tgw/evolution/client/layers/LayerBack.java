package tgw.evolution.client.layers;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.entity.Pose;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.item.ItemStack;
import net.minecraft.util.HandSide;
import tgw.evolution.events.ClientEvents;

public class LayerBack extends LayerRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>> {

    public LayerBack(IEntityRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>> playerRenderer) {
        super(playerRenderer);
    }

    @Override
    public void render(AbstractClientPlayerEntity player,
                       float limbSwing,
                       float limbSwingAmount,
                       float partialTicks,
                       float ageInTicks,
                       float headYaw,
                       float headPitch,
                       float scale) {
        if (player.isWearing(PlayerModelPart.CAPE) && player.getLocationCape() != null) {
            return;
        }
        ItemStack backStack = ClientEvents.BACK_ITEMS.getOrDefault(player.getEntityId(), ItemStack.EMPTY);
        if (!backStack.isEmpty()) {
            int sideOffset = player.getPrimaryHand() == HandSide.RIGHT ? 1 : -1;
            GlStateManager.pushMatrix();
            if (player.getPose() == Pose.SNEAKING) {
                GlStateManager.translatef(0.0f, 0.2f, 0.0f);
            }
            this.translateToBody();
            GlStateManager.translatef(-sideOffset * 0.1f, 0.25f, 0.17f);
            GlStateManager.scalef(sideOffset * 0.75f, 0.75f, 0.75f);
            Minecraft.getInstance().getItemRenderer().renderItem(backStack, player, ItemCameraTransforms.TransformType.GUI, false);
            GlStateManager.popMatrix();
        }
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }

    private void translateToBody() {
        this.getEntityModel().bipedBody.postRender(0.062_5F);
    }
}
