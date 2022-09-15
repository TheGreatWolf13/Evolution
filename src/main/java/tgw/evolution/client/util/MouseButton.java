package tgw.evolution.client.util;

import org.intellij.lang.annotations.MagicConstant;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static org.lwjgl.glfw.GLFW.*;

@Retention(RetentionPolicy.SOURCE)
@MagicConstant(intValues = {GLFW_MOUSE_BUTTON_1,
                            GLFW_MOUSE_BUTTON_2,
                            GLFW_MOUSE_BUTTON_3,
                            GLFW_MOUSE_BUTTON_4,
                            GLFW_MOUSE_BUTTON_5,
                            GLFW_MOUSE_BUTTON_6,
                            GLFW_MOUSE_BUTTON_7,
                            GLFW_MOUSE_BUTTON_8})
public @interface MouseButton {
}
