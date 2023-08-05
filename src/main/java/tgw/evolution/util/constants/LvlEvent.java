package tgw.evolution.util.constants;

import org.intellij.lang.annotations.MagicConstant;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE_USE)
@MagicConstant(valuesFromClass = net.minecraft.world.level.block.LevelEvent.class)
public @interface LvlEvent {
}
