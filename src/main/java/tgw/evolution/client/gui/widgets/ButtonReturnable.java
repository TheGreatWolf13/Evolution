package tgw.evolution.client.gui.widgets;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import tgw.evolution.client.util.MouseButton;

public class ButtonReturnable extends Button {

    private @MouseButton int button;

    public ButtonReturnable(int x, int y, int width, int height, Component message, OnPress onPress) {
        super(x, y, width, height, message, onPress);
    }

    public @MouseButton int getLastButton() {
        return this.button;
    }

    @Override
    protected boolean isValidClickButton(@MouseButton int button) {
        this.button = button;
        return button == GLFW.GLFW_MOUSE_BUTTON_1 || button == GLFW.GLFW_MOUSE_BUTTON_2;
    }
}
