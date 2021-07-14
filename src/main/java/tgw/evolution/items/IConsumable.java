package tgw.evolution.items;

import com.google.common.collect.Lists;
import net.minecraft.item.UseAction;
import net.minecraft.potion.EffectInstance;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public interface IConsumable {

    List<Pair<EffectInstance, Float>> EMPTY = Lists.newArrayList();

    /**
     * @return The time to consume the item, in ticks.
     */
    int getConsumeTime();

    /**
     * @return A list made of pairs. Each pair contains an effect this item can apply paired with its chance, in float.
     */
    List<Pair<EffectInstance, Float>> getEffects();

    /**
     * @return The animation to use while consuming.
     */
    UseAction getUseAnimation();
}
