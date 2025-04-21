package tgw.evolution.hooks.asm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to mark constructors in Mixin classes that should be created at runtime. Also used to mark methods whose signature matches such constructors to make it possible to access the constructors created at runtime.
 */
@Target({ElementType.CONSTRUCTOR, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface NewConstructor {
}
