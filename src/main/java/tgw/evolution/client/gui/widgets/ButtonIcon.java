package tgw.evolution.client.gui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import tgw.evolution.client.util.Blending;
import tgw.evolution.init.EvolutionResources;

@OnlyIn(Dist.CLIENT)
public class ButtonIcon extends Button {
    private final Component label;
    private final int u;
    private final int v;

    public ButtonIcon(int x, int y, int width, int u, int v, Component label, OnPress pressedAction) {
        this(x, y, width, u, v, label, pressedAction, (b, ms, mx, my) -> {
        });
    }

    public ButtonIcon(int x, int y, int u, int v, OnPress onPress, OnTooltip onTooltip) {
        this(x, y, 20, u, v, TextComponent.EMPTY, onPress, onTooltip);
    }

    public ButtonIcon(int x, int y, int width, int u, int v, Component label, OnPress onPress, OnTooltip onTooltip) {
        super(x, y, width, 20, TextComponent.EMPTY, onPress, onTooltip);
        this.label = label;
        this.u = u;
        this.v = v;
    }

    @Override
    public void renderButton(PoseStack matrices, int mouseX, int mouseY, float partialTicks) {
        super.renderButton(matrices, mouseX, mouseY, partialTicks);
        Minecraft mc = Minecraft.getInstance();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, EvolutionResources.GUI_ICONS);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        Blending.DEFAULT.apply();
        int contentWidth = 10 + mc.font.width(this.label) + (!this.label.getString().isEmpty() ? 4 : 0);
        boolean renderIcon = contentWidth <= this.width;
        if (!renderIcon) {
            contentWidth = mc.font.width(this.label);
        }
        int iconX = this.x + (this.width - contentWidth) / 2;
        int iconY = this.y + 5;
        float brightness = this.active ? 1.0F : 0.5F;
        if (renderIcon) {
            RenderSystem.setShaderColor(brightness, brightness, brightness, this.alpha);
            blit(matrices, iconX, iconY, this.getBlitOffset(), this.u, this.v, 11, 11, 256, 256);
        }
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        int textColor = this.getFGColor() | Mth.ceil(this.alpha * 255.0F) << 24;
        drawString(matrices, mc.font, this.label, iconX + 14, iconY + 1, textColor);
    }
}
