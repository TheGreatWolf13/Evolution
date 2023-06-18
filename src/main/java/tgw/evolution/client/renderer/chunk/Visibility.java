package tgw.evolution.client.renderer.chunk;

import org.intellij.lang.annotations.MagicConstant;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
@MagicConstant(valuesFromClass = Visibility.class)
public @interface Visibility {

    int OUTSIDE = 0;
    int INTERSECT = 1;
    int INSIDE = 2;
}
