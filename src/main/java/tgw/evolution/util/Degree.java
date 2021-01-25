package tgw.evolution.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * The annotated method or field is an angle value given in degrees.
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
public @interface Degree {
}
