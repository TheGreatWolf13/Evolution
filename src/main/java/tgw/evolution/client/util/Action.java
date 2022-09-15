package tgw.evolution.client.util;

import org.intellij.lang.annotations.MagicConstant;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static org.lwjgl.glfw.GLFW.*;

@Retention(RetentionPolicy.SOURCE)
@MagicConstant(intValues = {GLFW_RELEASE, GLFW_PRESS, GLFW_REPEAT})
public @interface Action {
}
