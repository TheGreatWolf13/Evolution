package tgw.evolution.client.gui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import tgw.evolution.client.renderer.RenderHelper;
import tgw.evolution.client.util.Blending;
import tgw.evolution.init.EvolutionResources;

@OnlyIn(Dist.CLIENT)
public class ButtonIcon extends Button {
    private static final OnTooltip TOOLTIP = (b, m, mx, my) -> {
    };
    private final int u;
    private final int v;

    public ButtonIcon(int x, int y, int u, int v, OnPress pressedAction) {
        this(x, y, u, v, pressedAction, TOOLTIP);
    }

    public ButtonIcon(int x, int y, int u, int v, OnPress onPress, OnTooltip onTooltip) {
        super(x, y, 20, 20, TextComponent.EMPTY, onPress, onTooltip);
        this.u = u;
        this.v = v;
    }

    @Override
    public void renderButton(PoseStack matrices, int mouseX, int mouseY, float partialTicks) {
        super.renderButton(matrices, mouseX, mouseY, partialTicks);
        RenderSystem.setShader(RenderHelper.SHADER_POSITION_TEX);
        RenderSystem.setShaderTexture(0, EvolutionResources.GUI_ICONS);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        Blending.DEFAULT.apply();
        float brightness = this.active ? 1.0F : 0.5F;
        RenderSystem.setShaderColor(brightness, brightness, brightness, this.alpha);
        blit(matrices, this.x + 4, this.y + 4, this.getBlitOffset(), this.u, this.v, 12, 12, 256, 256);
    }
}
