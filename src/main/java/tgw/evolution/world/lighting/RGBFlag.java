package tgw.evolution.world.lighting;

import org.intellij.lang.annotations.MagicConstant;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE_USE)
@MagicConstant(flagsFromClass = RGBFlag.class)
public @interface RGBFlag {

    int RED = 1 << RGB.RED;
    int GREEN = 1 << RGB.GREEN;
    int BLUE = 1 << RGB.BLUE;
}
