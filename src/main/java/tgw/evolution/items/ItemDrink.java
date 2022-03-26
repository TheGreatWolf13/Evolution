package tgw.evolution.items;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.UseAnim;
import tgw.evolution.util.Object2FloatPair;

import java.util.List;

public class ItemDrink extends ItemGenericConsumable implements IDrink {

    private final UseAnim anim;
    private final int consumeTime;
    private final List<Object2FloatPair<MobEffectInstance>> effects;
    private final int thirst;

    public ItemDrink(Item.Properties properties, DrinkProperties drink) {
        super(properties);
        this.consumeTime = drink.getConsumeTime();
        this.effects = drink.getEffects();
        this.thirst = drink.getThirst();
        this.anim = drink.getAnim();
    }

    @Override
    public int getConsumeTime() {
        return this.consumeTime;
    }

    @Override
    public List<Object2FloatPair<MobEffectInstance>> getEffects() {
        return this.effects;
    }

    @Override
    public int getThirst() {
        return this.thirst;
    }

    @Override
    public UseAnim getUseAnimation() {
        return this.anim;
    }
}
