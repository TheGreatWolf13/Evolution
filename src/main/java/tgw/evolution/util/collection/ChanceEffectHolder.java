package tgw.evolution.util.collection;

import it.unimi.dsi.fastutil.ints.Int2FloatFunction;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.function.IntFunction;

public class ChanceEffectHolder extends EffectHolder {

    private final Int2FloatFunction chanceMaker;

    public ChanceEffectHolder(int minAmplifier,
                              MobEffect effect, Int2FloatFunction chanceMaker,
                              IntFunction<@Nullable MobEffectInstance> effectMaker) {
        super(minAmplifier, effect, effectMaker);
        this.chanceMaker = chanceMaker;
    }

    @Override
    public boolean shouldApply(int amplifier, LivingEntity entity) {
        if (!super.shouldApply(amplifier, entity)) {
            return false;
        }
        float chance = this.chanceMaker.get(amplifier);
        if (chance >= 1.0f) {
            return true;
        }
        return entity.getRandom().nextFloat() < chance;
    }

    @Override
    public boolean shouldReapply(int amplifier, LivingEntity entity) {
        if (!super.shouldReapply(amplifier, entity)) {
            return false;
        }
        float chance = this.chanceMaker.get(amplifier);
        if (chance >= 1.0f) {
            return true;
        }
        return entity.getRandom().nextFloat() < chance;
    }
}
