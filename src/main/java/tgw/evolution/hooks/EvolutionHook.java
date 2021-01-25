package tgw.evolution.hooks;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * The annotated method is an entry-point hook applied through ASM.
 */
@Target(ElementType.METHOD)
public @interface EvolutionHook {
}
