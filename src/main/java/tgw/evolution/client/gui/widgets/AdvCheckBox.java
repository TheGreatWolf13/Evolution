package tgw.evolution.client.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import tgw.evolution.client.gui.GUIUtils;

public class AdvCheckBox extends Button {

    private final int boxWidth;
    private final boolean leftText;
    private final OnCheck onCheck;
    private boolean isChecked;

    public AdvCheckBox(int xPos, int yPos, Component displayString, boolean isChecked, boolean leftText, OnCheck onCheck) {
        super(xPos, yPos, Minecraft.getInstance().font.width(displayString) + 2 + 11, 11, displayString, b -> {});
        this.isChecked = isChecked;
        this.boxWidth = 11;
        this.height = 11;
        this.width = this.boxWidth + 2 + Minecraft.getInstance().font.width(displayString);
        this.leftText = leftText;
        this.onCheck = onCheck;
    }

    public AdvCheckBox(int xPos, int yPos, Component displayString, boolean isChecked, OnCheck onCheck) {
        this(xPos, yPos, displayString, isChecked, false, onCheck);
    }

    public boolean isChecked() {
        return this.isChecked;
    }

    @Override
    public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_) {
        return false;
    }

    @Override
    public void onPress() {
        this.isChecked = !this.isChecked;
        this.onCheck.onCheck(this);
    }

    @Override
    public void renderButton(PoseStack matrices, int mouseX, int mouseY, float partial) {
        if (this.visible) {
            Minecraft mc = Minecraft.getInstance();
            this.isHovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.boxWidth && mouseY < this.y + this.height;
            GUIUtils.drawContinuousTexturedBox(matrices,
                                               WIDGETS_LOCATION,
                                               this.leftText ? this.x + this.width - this.boxWidth : this.x,
                                               this.y,
                                               0,
                                               46,
                                               this.boxWidth,
                                               this.height,
                                               200,
                                               20,
                                               2,
                                               3,
                                               2,
                                               2,
                                               0);
            int color = 0xe0_e0e0;
            if (!this.active) {
                color = 0xa0_a0a0;
            }
            if (this.isChecked) {
                drawCenteredString(matrices,
                                   mc.font,
                                   "x",
                                   (this.leftText ? this.x + this.width - this.boxWidth : this.x) + this.boxWidth / 2 + 1,
                                   this.y + 1,
                                   0xe0_e0e0);
            }
            drawString(matrices, mc.font, this.getMessage(), this.leftText ? this.x : this.x + this.boxWidth + 2, this.y + 2, color);
        }
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
    }

    public void setIsChecked(boolean isChecked) {
        this.isChecked = isChecked;
        this.onCheck.onCheck(this);
    }

    public interface OnCheck {
        void onCheck(AdvCheckBox box);
    }
}
