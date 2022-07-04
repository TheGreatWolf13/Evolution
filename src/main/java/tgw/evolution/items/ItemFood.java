package tgw.evolution.items;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.UseAnim;
import tgw.evolution.util.Object2FloatPair;

import java.util.List;

public class ItemFood extends ItemGenericConsumable implements IFood {

    private final UseAnim action;
    private final int consumeTime;
    private final List<Object2FloatPair<MobEffectInstance>> effects;
    private final int hunger;

    public ItemFood(Item.Properties properties, FoodProperties food) {
        super(properties);
        this.hunger = food.getHunger();
        this.consumeTime = food.getConsumeTime();
        this.action = food.getAnim();
        this.effects = food.getEffects();
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
    public int getHunger() {
        return this.hunger;
    }

    @Override
    public UseAnim getUseAnimation() {
        return this.action;
    }
}