package tgw.evolution.client.gui.widgets;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import tgw.evolution.client.util.MouseButton;

public class AdvEditBox extends EditBox {

    public AdvEditBox(Font font, int x, int y, int width, int height, Component message) {
        super(font, x, y, width, height, message);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, @MouseButton int button) {
        boolean mouseClicked = super.mouseClicked(mouseX, mouseY, button);
        if (!mouseClicked && this.isFocused() && button == GLFW.GLFW_MOUSE_BUTTON_2) {
            this.setValue("");
            return true;
        }
        return mouseClicked;
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
