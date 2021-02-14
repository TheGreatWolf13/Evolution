package tgw.evolution.client.layers;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.item.ItemStack;
import net.minecraft.util.HandSide;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import tgw.evolution.events.ClientEvents;

@OnlyIn(Dist.CLIENT)
public class LayerBelt extends LayerRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>> {

    public LayerBelt(IEntityRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>> playerRenderer) {
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
        ItemStack beltStack = ClientEvents.BELT_ITEMS.getOrDefault(player.getEntityId(), ItemStack.EMPTY);
        if (!beltStack.isEmpty()) {
            int sideOffset = player.getPrimaryHand() == HandSide.RIGHT ? -1 : 1;
            GlStateManager.pushMatrix();
            if (player.isSneaking()) {
                GlStateManager.translatef(0.0f, 0.2f, 0.0f);
            }
            this.translateToBody();
            GlStateManager.rotatef(-90.0f, 0.0f, 1.0f, 0.0f);
            GlStateManager.translatef(0.1f, 0.85f, sideOffset * 0.28f);
            GlStateManager.scalef(0.75f, 0.75f, 0.75f);
            Minecraft.getInstance()
                     .getItemRenderer()
                     .renderItem(beltStack, player, ItemCameraTransforms.TransformType.NONE, player.getPrimaryHand() == HandSide.LEFT);
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
