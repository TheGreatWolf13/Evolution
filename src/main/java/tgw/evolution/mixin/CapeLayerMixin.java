package tgw.evolution.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(CapeLayer.class)
public abstract class CapeLayerMixin extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    public CapeLayerMixin(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> pRenderer) {
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
                       AbstractClientPlayer entity,
                       float limbSwing,
                       float limbSwingAmount,
                       float partialTicks,
                       float ageInTicks,
                       float netHeadYaw,
                       float headPitch) {
        if (entity.isCapeLoaded() &&
            !entity.isInvisible() &&
            entity.isModelPartShown(PlayerModelPart.CAPE) &&
            entity.getCloakTextureLocation() != null) {
            ItemStack chestStack = entity.getItemBySlot(EquipmentSlot.CHEST);
            if (!chestStack.is(Items.ELYTRA)) {
                VertexConsumer vertex = buffer.getBuffer(RenderType.entitySolid(entity.getCloakTextureLocation()));
                this.getParentModel().renderCloak(matrices, vertex, light, OverlayTexture.NO_OVERLAY);
            }
        }
    }
}
