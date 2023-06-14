package tgw.evolution.util.constants;

import org.intellij.lang.annotations.MagicConstant;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
@MagicConstant(valuesFromClass = net.minecraft.world.level.block.LevelEvent.class)
public @interface LvlEvent {
}
