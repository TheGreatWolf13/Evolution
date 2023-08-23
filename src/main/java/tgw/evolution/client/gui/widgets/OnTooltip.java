package tgw.evolution.client.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;

@FunctionalInterface
public interface OnTooltip {

    void onTooltip(PoseStack matrices, int mouseX, int mouseY);
}
