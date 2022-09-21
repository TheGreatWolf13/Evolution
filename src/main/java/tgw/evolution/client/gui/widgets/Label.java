package tgw.evolution.client.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.client.text.CappedComponent;
import tgw.evolution.util.math.MathHelper;

public class Label {

    private final CappedComponent capped;
    private final MutableComponent display;
    private final boolean hasAddendum;
    private final OnTooltip onTooltip;
    private final boolean shadow;
    private final Component tooltip;

    public Label(Component title, Component desc, OnTooltip onTooltip) {
        this(title, desc, null, true, onTooltip, ChatFormatting.WHITE, ChatFormatting.GRAY, ChatFormatting.WHITE);
    }

    public Label(Component title,
                 Component desc,
                 @Nullable Component addendum,
                 boolean shadow,
                 OnTooltip onTooltip,
                 ChatFormatting titleColor,
                 ChatFormatting descColor,
                 ChatFormatting tooltipColor) {
        this.onTooltip = onTooltip;
        this.shadow = shadow;
        MutableComponent copy = title.copy();
        this.capped = new CappedComponent(desc, 50, copy);
        if (addendum != null) {
            this.tooltip = desc.copy().withStyle(tooltipColor).append(addendum.copy().withStyle(tooltipColor));
            this.hasAddendum = true;
        }
        else {
            this.tooltip = desc.copy().withStyle(tooltipColor);
            this.hasAddendum = false;
        }
        this.display = copy.withStyle(titleColor).append(this.capped.withStyle(descColor));
    }

    public Component getTooltip() {
        return this.tooltip;
    }

    public void render(Font font, PoseStack matrices, int x, int y, int maxWidth, double mouseX, double mouseY) {
        this.capped.setWidth(maxWidth, this.display);
        if (this.shadow) {
            font.drawShadow(matrices, this.display, x, y, 0xFF_FFFF);
        }
        else {
            font.draw(matrices, this.display, x, y, 0xFF_FFFF);
        }
        if ((this.hasAddendum || this.capped.isCapped()) && MathHelper.isMouseInArea(mouseX, mouseY, x, y, font.width(this.display), 9)) {
            this.onTooltip.onTooltip(this);
        }
    }

    public interface OnTooltip {
        void onTooltip(Label label);
    }
}
