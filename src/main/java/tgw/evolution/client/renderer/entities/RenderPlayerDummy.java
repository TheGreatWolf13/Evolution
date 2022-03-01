package tgw.evolution.client.renderer.entities;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.network.chat.Component;

public class RenderPlayerDummy extends PlayerRenderer {

    public RenderPlayerDummy(EntityRendererProvider.Context context, boolean smallArms) {
        super(context, smallArms);
    }

    @Override
    protected void renderNameTag(AbstractClientPlayer player, Component name, PoseStack matrices, MultiBufferSource buffer, int packedLight) {
    }
}
