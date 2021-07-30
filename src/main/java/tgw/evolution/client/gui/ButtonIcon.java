package tgw.evolution.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.Button;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.util.MathHelper;

public class ButtonIcon extends Button {
    private final int u;
    private final int v;

    public ButtonIcon(int x, int y, int width, int height, int u, int v, IPressable pressedAction) {
        super(x, y, width, height, "", pressedAction);
        this.u = u;
        this.v = v;
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTicks) {
        Minecraft mc = Minecraft.getInstance();
        FontRenderer font = mc.fontRenderer;
        mc.getTextureManager().bindTexture(WIDGETS_LOCATION);
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, this.alpha);
        int vOffset = this.getYImage(this.isHovered());
        GlStateManager.enableBlend();
        GlStateManager.enableDepthTest();
        this.blit(this.x, this.y, 0, 46 + vOffset * 20, this.width / 2, this.height);
        this.blit(this.x + this.width / 2, this.y, 200 - this.width / 2, 46 + vOffset * 20, this.width / 2, this.height);
        this.renderBg(mc, mouseX, mouseY);
        int color = this.getFGColor();
        this.drawCenteredString(font,
                                this.getMessage(),
                                this.x + this.width / 2,
                                this.y + (this.height - 8) / 2,
                                color | MathHelper.ceil(this.alpha * 255.0F) << 24);
        mc.getTextureManager().bindTexture(EvolutionResources.GUI_ICONS);
        float brightness = this.active ? 1.0F : 0.5F;
        GlStateManager.color4f(brightness, brightness, brightness, this.alpha);
        blit(this.x + 5, this.y + 4, this.blitOffset, this.u, this.v, 11, 11, 256, 256);
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, this.alpha);
    }
}
