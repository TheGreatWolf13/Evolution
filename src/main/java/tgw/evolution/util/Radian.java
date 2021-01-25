package tgw.evolution.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * The annotated method or field is an angle value given in radians.
 */
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
public @interface Radian {
}
