package tgw.evolution.client.tooltip;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.client.renderer.ambient.DynamicLights;
import tgw.evolution.init.EvolutionResources;

public final class EvolutionTooltipRenderer implements ClientTooltipComponent {

    public static final EvolutionTooltipRenderer COLD = new EvolutionTooltipRenderer();
    public static final EvolutionTooltipRenderer COOLDOWN = new EvolutionTooltipRenderer();
    public static final EvolutionTooltipRenderer DAMAGE_BASIC = new EvolutionTooltipRenderer();
    public static final EvolutionTooltipRenderer DAMAGE_CHARGE = new EvolutionTooltipRenderer();
    public static final EvolutionTooltipRenderer DAMAGE_THROWN = new EvolutionTooltipRenderer();
    public static final EvolutionTooltipRenderer DMG_MULT_BASIC = new EvolutionTooltipRenderer();
    public static final EvolutionTooltipRenderer DMG_MULT_CHARGE = new EvolutionTooltipRenderer();
    public static final EvolutionTooltipRenderer DMG_MULT_THROWN = new EvolutionTooltipRenderer();
    public static final EvolutionTooltipRenderer DRINK = new EvolutionTooltipRenderer();
    public static final EvolutionTooltipRenderer DURABILITY = new EvolutionTooltipRenderer();
    public static final EvolutionTooltipRenderer[] DURABILITY_PARTS = new EvolutionTooltipRenderer[4];
    public static final EvolutionTooltipRenderer FOLLOW_UP = new EvolutionTooltipRenderer();
    public static final EvolutionTooltipRenderer FOOD = new EvolutionTooltipRenderer();
    public static final EvolutionTooltipRenderer HEAT = new EvolutionTooltipRenderer();
    public static final EvolutionTooltipRenderer INFO = new EvolutionTooltipRenderer();
    public static final EvolutionTooltipRenderer MASS = new EvolutionTooltipRenderer();
    public static final EvolutionTooltipRenderer[] MASS_PARTS = new EvolutionTooltipRenderer[4];
    public static final EvolutionTooltipRenderer MINING = new EvolutionTooltipRenderer();
    public static final EvolutionTooltipRenderer PRECISION = new EvolutionTooltipRenderer();
    public static final EvolutionTooltipRenderer STRUCTURAL_ARCH_COST = new EvolutionTooltipRenderer();
    public static final EvolutionTooltipRenderer STRUCTURAL_BEAM_COST = new EvolutionTooltipRenderer();
    public static final EvolutionTooltipRenderer STRUCTURAL_INTEGRITY = new EvolutionTooltipRenderer();
    public static final EvolutionTooltipRenderer STRUCTURAL_TYPE = new EvolutionTooltipRenderer();
    public static final EvolutionTooltipRenderer THROW_SPEED = new EvolutionTooltipRenderer();

    static {
        for (int i = 0; i < 4; i++) {
            //noinspection ObjectAllocationInLoop
            DURABILITY_PARTS[i] = new EvolutionTooltipRenderer();
            //noinspection ObjectAllocationInLoop
            MASS_PARTS[i] = new EvolutionTooltipRenderer();
        }
    }

    private @Nullable ITooltip tooltip;

    private EvolutionTooltipRenderer() {
    }

    @Override
    public int getHeight() {
        return 9;
    }

    @Override
    public int getWidth(Font font) {
        if (this.tooltip != null) {
            return this.tooltip.getOffsetX() + font.width(this.tooltip.getText());
        }
        return 9;
    }

    @Override
    public void renderImage(Font font, int mouseX, int mouseY, PoseStack matrices, ItemRenderer itemRenderer, int blitOffset) {
        if (this.tooltip == null) {
            return;
        }
        Screen gui = Minecraft.getInstance().screen;
        if (gui == null) {
            return;
        }
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderTexture(0, EvolutionResources.GUI_ICONS);
        GuiComponent.blit(matrices,
                          mouseX + this.tooltip.getOffsetX() - 12,
                          mouseY - 1,
                          blitOffset,
                          this.tooltip.getIconX(),
                          this.tooltip.getIconY(),
                          9,
                          9,
                          256,
                          256);
        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();
    }

    @Override
    public void renderText(Font font, int x, int y, Matrix4f matrix, MultiBufferSource.BufferSource buffer) {
        if (this.tooltip != null) {
            font.drawInBatch(this.tooltip.getText(), x + this.tooltip.getOffsetX(), y, 0xffff_ffff, true, matrix, buffer, false, 0x0, DynamicLights.FULL_LIGHTMAP);
        }
    }

    public EvolutionTooltipRenderer setTooltip(ITooltip tooltip) {
        this.tooltip = tooltip;
        return this;
    }
}
