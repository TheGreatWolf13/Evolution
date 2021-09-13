package tgw.evolution.client.gui.widgets;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.init.EvolutionTexts;
import tgw.evolution.util.MathHelper;

@OnlyIn(Dist.CLIENT)
public class ButtonIcon extends Button {
    private final int u;
    private final int v;

    public ButtonIcon(int x, int y, int width, int height, int u, int v, IPressable pressedAction) {
        super(x, y, width, height, EvolutionTexts.EMPTY, pressedAction);
        this.u = u;
        this.v = v;
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float partialTicks) {
        Minecraft mc = Minecraft.getInstance();
        FontRenderer font = mc.font;
        mc.getTextureManager().bind(WIDGETS_LOCATION);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, this.alpha);
        int vOffset = this.getYImage(this.isHovered());
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        this.blit(matrices, this.x, this.y, 0, 46 + vOffset * 20, this.width / 2, this.height);
        this.blit(matrices, this.x + this.width / 2, this.y, 200 - this.width / 2, 46 + vOffset * 20, this.width / 2, this.height);
        this.renderBg(matrices, mc, mouseX, mouseY);
        int color = this.getFGColor();
        drawCenteredString(matrices,
                           font,
                           this.getMessage(),
                           this.x + this.width / 2,
                           this.y + (this.height - 8) / 2,
                           color | MathHelper.ceil(this.alpha * 255.0F) << 24);
        mc.getTextureManager().bind(EvolutionResources.GUI_ICONS);
        float brightness = this.active ? 1.0F : 0.5F;
        RenderSystem.color4f(brightness, brightness, brightness, this.alpha);
        blit(matrices, this.x + 5, this.y + 4, this.getBlitOffset(), this.u, this.v, 11, 11, 256, 256);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, this.alpha);
    }
}
