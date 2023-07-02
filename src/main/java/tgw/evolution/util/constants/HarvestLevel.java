package tgw.evolution.util.constants;

import org.intellij.lang.annotations.MagicConstant;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE_USE)
@MagicConstant(valuesFromClass = HarvestLevel.class)
public @interface HarvestLevel {

    int HAND = 0;
    int STONE = 1;
    int LOW_METAL = 2;
    int COPPER = 3;
    int BRONZE = 4;
    int IRON = 5;
    int UNBREAKABLE = 1_000;
}
