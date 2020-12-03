package tgw.evolution.client.renderer.item;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.item.ItemStack;
import tgw.evolution.client.models.entities.ModelSpear;
import tgw.evolution.items.ISpear;
import tgw.evolution.items.ItemJavelin;

public class RenderStackItemJavelin extends ItemStackTileEntityRenderer {

    private final ModelSpear spear = new ModelSpear();

    @Override
    public void renderByItem(ItemStack stack) {
        if (stack.getItem() instanceof ItemJavelin) {
            Minecraft.getInstance().getTextureManager().bindTexture(((ISpear) stack.getItem()).getTexture());
            GlStateManager.pushMatrix();
            GlStateManager.scalef(1.0F, -1.0F, -1.0F);
            GlStateManager.translatef(0.0f, -0.1f, 0.0f);
            GlStateManager.rotatef(180.0F, 0.0F, 0.0F, 1.0F);
            this.spear.render();
            GlStateManager.popMatrix();
        }
    }
}
