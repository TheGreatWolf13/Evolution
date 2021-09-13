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
import net.minecraft.item.ItemStack;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import tgw.evolution.events.ClientEvents;

@OnlyIn(Dist.CLIENT)
public class LayerBelt extends LayerRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>> {

    public LayerBelt(IEntityRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>> playerRenderer) {
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
        ItemStack beltStack = ClientEvents.BELT_ITEMS.getOrDefault(player.getId(), ItemStack.EMPTY);
        if (!beltStack.isEmpty()) {
            int sideOffset = player.getMainArm() == HandSide.RIGHT ? -1 : 1;
            matrices.pushPose();
            this.getParentModel().body.translateAndRotate(matrices);
            matrices.mulPose(Vector3f.YP.rotationDegrees(-90));
            matrices.translate(0.1, 0.85, sideOffset * 0.28);
            matrices.scale(0.75f, 0.75f, 0.75f);
            Minecraft.getInstance()
                     .getItemRenderer()
                     .renderStatic(player,
                                   beltStack,
                                   ItemCameraTransforms.TransformType.NONE,
                                   player.getMainArm() == HandSide.LEFT,
                                   matrices,
                                   buffer,
                                   player.level,
                                   packetLight,
                                   OverlayTexture.NO_OVERLAY);
            matrices.popPose();
        }
    }
}
