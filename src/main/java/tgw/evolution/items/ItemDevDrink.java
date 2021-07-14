package tgw.evolution.items;

import net.minecraft.item.UseAction;
import net.minecraft.potion.EffectInstance;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class ItemDevDrink extends ItemGenericConsumable implements IDrink {

    public ItemDevDrink(Properties properties) {
        super(properties);
    }

    @Override
    public int getConsumeTime() {
        return 32;
    }

    @Override
    public List<Pair<EffectInstance, Float>> getEffects() {
        return EMPTY;
    }

    @Override
    public int getThirst() {
        return 250;
    }

    @Override
    public UseAction getUseAnimation() {
        return UseAction.DRINK;
    }
}
