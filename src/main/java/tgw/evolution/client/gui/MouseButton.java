package tgw.evolution.client.gui;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;

@OnlyIn(Dist.CLIENT)
public enum MouseButton {
    LEFT(0),
    RIGHT(1);

    private final int id;

    MouseButton(int id) {
        this.id = id;
    }

    @Nullable
    public static MouseButton fromGLFW(int glfw) {
        switch (glfw) {
            case GLFW.GLFW_MOUSE_BUTTON_LEFT:
                return LEFT;
            case GLFW.GLFW_MOUSE_BUTTON_RIGHT:
                return RIGHT;
        }
        return null;
    }

    public int getValue() {
        return this.id;
    }
}
