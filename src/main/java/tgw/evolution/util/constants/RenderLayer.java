package tgw.evolution.util.constants;

import org.intellij.lang.annotations.MagicConstant;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE_USE)
@MagicConstant(valuesFromClass = RenderLayer.class)
public @interface RenderLayer {

    int SOLID = 0;
    int CUTOUT_MIPPED = 1;
    int CUTOUT = 2;
    int TRANSLUCENT = 3;
    int TRIPWIRE = 4;
}
