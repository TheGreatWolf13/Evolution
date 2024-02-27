package tgw.evolution.client.util;

import org.intellij.lang.annotations.MagicConstant;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE_USE)
@MagicConstant(valuesFromClass = Shader.class)
public @interface Shader {

    int CYCLE = -3;
    int QUERY = -2;
    int TOGGLE = -1;
    int CLEAR = 0;
    int MOTION_BLUR = 1;
    int DESATURATE_25 = 25;
    int DESATURATE_50 = 50;
    int DESATURATE_75 = 75;
    int TEST = 99;
}
