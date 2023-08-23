package tgw.evolution.client.gui.widgets;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import tgw.evolution.client.gui.ScreenEvolution;
import tgw.evolution.client.util.MouseButton;
import tgw.evolution.util.math.MathHelper;

public class AdvEditBox extends EditBox {

    protected boolean isInvalid;

    public AdvEditBox(Font font, int x, int y, int width, int height, Component message) {
        super(font, x, y, width, height, message);
    }

    public boolean isInvalid() {
        return this.isInvalid;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, @MouseButton int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (this.visible && this.active && button == GLFW.GLFW_MOUSE_BUTTON_2) {
            if (MathHelper.isMouseInArea(mouseX, mouseY, this.x, this.y, this.width, this.height)) {
                this.setValue("");
                this.setFocus(true);
                return true;
            }
        }
        return false;
    }

    @Override
    public void setFocus(boolean focus) {
        super.setFocus(focus);
        if (focus) {
            Screen screen = this.getScreen();
            if (screen instanceof ScreenEvolution s) {
                s.setEditBox(this);
            }
        }
    }

    public void setInvalid(boolean invalid) {
        this.isInvalid = invalid;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.isFocused() && this.getValue().isEmpty()) {
            this.setSuggestion(this.getMessage().getString());
        }
        else {
            this.setSuggestion("");
        }
    }
}
