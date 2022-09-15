package tgw.evolution.client.util;

import org.intellij.lang.annotations.MagicConstant;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static org.lwjgl.glfw.GLFW.*;

@Retention(RetentionPolicy.SOURCE)
@MagicConstant(flags = {GLFW_MOD_SHIFT, GLFW_MOD_CONTROL, GLFW_MOD_ALT, GLFW_MOD_SUPER})
public @interface Modifiers {
}
