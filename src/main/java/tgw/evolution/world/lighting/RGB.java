package tgw.evolution.world.lighting;

import org.intellij.lang.annotations.MagicConstant;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@MagicConstant(valuesFromClass = RGB.class)
@Target(ElementType.TYPE_USE)
@Retention(RetentionPolicy.SOURCE)
public @interface RGB {

    int RED = 0;
    int GREEN = 1;
    int BLUE = 2;
}
