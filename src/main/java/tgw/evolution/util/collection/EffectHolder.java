package tgw.evolution.util.collection;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.function.IntFunction;

public class EffectHolder {

    private final MobEffect effect;
    private final IntFunction<@Nullable MobEffectInstance> effectMaker;
    private final int minAmplifier;

    public EffectHolder(int minAmplifier, MobEffect effect, IntFunction<@Nullable MobEffectInstance> effectMaker) {
        this.effect = effect;
        this.minAmplifier = minAmplifier;
        this.effectMaker = effectMaker;
    }

    public void apply(int amplifier, LivingEntity entity) {
        MobEffectInstance instance = this.effectMaker.apply(amplifier);
        if (instance != null) {
            entity.addEffect(instance);
        }
    }

    @Nullable
    public MobEffectInstance getInstance(int amplifier) {
        if (amplifier < this.minAmplifier) {
            return null;
        }
        return this.effectMaker.apply(amplifier);
    }

    public void remove(int amplifier, LivingEntity entity) {
        if (this.shouldApply(amplifier, entity)) {
            entity.removeEffect(this.effect);
        }
    }

    public boolean shouldApply(int amplifier, LivingEntity entity) {
        return amplifier >= this.minAmplifier;
    }

    public boolean shouldReapply(int amplifier, LivingEntity entity) {
        if (amplifier < this.minAmplifier) {
            return false;
        }
        return !entity.hasEffect(this.effect);
    }
}
