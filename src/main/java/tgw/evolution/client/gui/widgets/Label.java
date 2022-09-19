package tgw.evolution.client.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import tgw.evolution.util.math.MathHelper;

public class Label {

    private final ChatFormatting labelColor;
    private final OnTooltip onTooltip;
    private final ChatFormatting tooltipColor;
    private MutableComponent cutComponent;
    private MutableComponent fullComponent;
    private Component label;
    private Component tooltip;

    public Label(Component label, Component tooltip, OnTooltip onTooltip) {
        this(label, tooltip, onTooltip, ChatFormatting.WHITE, ChatFormatting.GRAY);
    }

    public Label(Component label, Component tooltip, OnTooltip onTooltip, ChatFormatting labelColor, ChatFormatting tooltipColor) {
        this.label = label.copy().withStyle(labelColor);
        this.tooltip = tooltip.copy().withStyle(tooltipColor);
        this.onTooltip = onTooltip;
        this.labelColor = labelColor;
        this.tooltipColor = tooltipColor;
        this.updateFullComponent();
    }

    public Component getTooltip() {
        return this.tooltip;
    }

    public void render(Font font, PoseStack matrices, int x, int y, int maxWidth, double mouseX, double mouseY) {
        if (font.width(this.fullComponent) > maxWidth) {
            this.updateCutComponent(font, maxWidth);
            GuiComponent.drawString(matrices, font, this.cutComponent, x, y, 0xFF_FFFF);
            if (MathHelper.isMouseInRange(mouseX, mouseY, x, y, x + maxWidth, y + 9)) {
                this.onTooltip.onTooltip(this);
            }
        }
        else {
            GuiComponent.drawString(matrices, font, this.fullComponent, x, y, 0xFF_FFFF);
        }
    }

    public void setLabel(Component label) {
        this.label = label;
        this.updateFullComponent();
    }

    public void setTooltip(Component tooltip) {
        this.tooltip = tooltip;
        this.updateFullComponent();
    }

    private void updateCutComponent(Font font, int width) {
        if (font.width(this.cutComponent) > width) {
            this.cutComponent = this.label.copy().withStyle(this.labelColor);
            String cutText = font.substrByWidth(this.tooltip, width - font.width(this.label) - 7).getString() + "...";
            this.cutComponent.append(new TextComponent(cutText).withStyle(this.tooltipColor));
        }
    }

    private void updateFullComponent() {
        this.fullComponent = this.label.copy().withStyle(this.labelColor).append(this.tooltip.copy().withStyle(this.tooltipColor));
        this.cutComponent = this.fullComponent;
    }

    public interface OnTooltip {
        void onTooltip(Label label);
    }
}
