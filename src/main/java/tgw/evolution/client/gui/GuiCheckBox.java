package tgw.evolution.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraftforge.fml.client.config.GuiUtils;

public class GuiCheckBox extends Button {
    private final int boxWidth;
    private boolean isChecked;

    public GuiCheckBox(int xPos, int yPos, String displayString, boolean isChecked) {
        super(xPos, yPos, Minecraft.getInstance().fontRenderer.getStringWidth(displayString) + 2 + 11, 11, displayString, b -> {
        });
        this.isChecked = isChecked;
        this.boxWidth = 11;
        this.height = 11;
        this.width = this.boxWidth + 2 + Minecraft.getInstance().fontRenderer.getStringWidth(displayString);
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
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partial) {
        if (this.visible) {
            Minecraft mc = Minecraft.getInstance();
            this.isHovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.boxWidth && mouseY < this.y + this.height;
            GuiUtils.drawContinuousTexturedBox(WIDGETS_LOCATION, this.x, this.y, 0, 46, this.boxWidth, this.height, 200, 20, 2, 3, 2, 2, 500);
            int color = 0xe0_e0e0;
            if (this.packedFGColor != 0) {
                color = this.packedFGColor;
            }
            else if (!this.active) {
                color = 0xa0_a0a0;
            }
            if (this.isChecked) {
                this.drawCenteredString(mc.fontRenderer, "x", this.x + this.boxWidth / 2 + 1, this.y + 1, 0xe0_e0e0);
            }
            this.drawString(mc.fontRenderer, this.getMessage(), this.x + this.boxWidth + 2, this.y + 2, color);
        }
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
    }

    public void setIsChecked(boolean isChecked) {
        this.isChecked = isChecked;
    }
}
