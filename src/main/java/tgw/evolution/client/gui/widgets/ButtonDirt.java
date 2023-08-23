package tgw.evolution.client.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import tgw.evolution.client.gui.GUIUtils;

public class ButtonDirt extends ButtonSelectable {

    public ButtonDirt(int x, int y, int width, int height, Component name, OnPress onPress) {
        super(x, y, width, height, name, onPress);
    }

    public ButtonDirt(int x, int y, int width, int height, Component name, OnPress onPress, ButtonGroup group) {
        super(x, y, width, height, name, onPress, group);
    }

    @Override
    public void renderButton(PoseStack matrices, int mouseX, int mouseY, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        boolean selected = this.isSelected();
        GUIUtils.renderDirtBackground(this.x + 1, this.y + 1, this.x + this.width - 1, this.y + this.height - 1, selected ? 64 : 16);
        int color = (selected ? 0xFF_FFFF : 0xA0_A0A0) | Mth.ceil(this.alpha * 255.0F) << 24;
        if (selected) {
            GUIUtils.drawRect(this.x, this.y, this.x + this.width, this.y + this.height, 1, color);
        }
        //Message
        Font font = mc.font;
        Component message = this.getMessage();
        int messageWidth = font.width(message);
        int widthThatFits = this.width - 4;
        if (messageWidth > widthThatFits) {
            double d = Util.getMillis() / 1_000.0;
            int delta = messageWidth - widthThatFits;
            double mult = 0.51;
            if (delta < 2) {
                delta = 2;
                mult = 1;
            }
            double e = Math.sin(Mth.HALF_PI * Math.cos(d)) * mult;
            GUIUtils.enableScissor(this.x + 2, this.y + 2, this.x + this.width - 3, this.y + this.height - 2);
            drawCenteredString(matrices, font, message, this.x + this.width / 2 - (int) (e * delta), this.y + (this.height - 8) / 2, color);
            GUIUtils.disableScissor();
        }
        else {
            drawCenteredString(matrices, font, message, this.x + this.width / 2, this.y + (this.height - 8) / 2, color);
        }
    }
}
