package tgw.evolution.client.renderer.entities;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.util.text.ITextComponent;

public class RenderPlayerDummy extends PlayerRenderer {

    public RenderPlayerDummy(EntityRendererManager manager, boolean smallArms) {
        super(manager, smallArms);
    }

    @Override
    protected void renderNameTag(AbstractClientPlayerEntity player,
                                 ITextComponent name,
                                 MatrixStack matrices,
                                 IRenderTypeBuffer buffer,
                                 int packedLight) {
    }
}
