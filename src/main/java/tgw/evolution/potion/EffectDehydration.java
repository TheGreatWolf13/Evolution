package tgw.evolution.potion;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierManager;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraft.potion.Effects;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.init.EvolutionEffects;
import tgw.evolution.patches.IEffectInstancePatch;

public class EffectDehydration extends Effect {

    public EffectDehydration() {
        super(EffectType.HARMFUL, 0);
    }

    @Override
    public void addAttributeModifiers(LivingEntity entity, AttributeModifierManager attributes, int amplifier) {
        if (amplifier >= 1) {
            entity.addEffect(IEffectInstancePatch.newInfinite(Effects.MOVEMENT_SLOWDOWN, amplifier - 1, false, false, false));
        }
        super.addAttributeModifiers(entity, attributes, amplifier);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (amplifier >= 2) {
            entity.hurt(EvolutionDamage.DEHYDRATION, 1.0f);
        }
        if (amplifier >= 1 && !entity.hasEffect(Effects.MOVEMENT_SLOWDOWN)) {
            entity.addEffect(IEffectInstancePatch.newInfinite(Effects.MOVEMENT_SLOWDOWN, amplifier - 1, false, false, false));
        }
        if (!entity.hasEffect(EvolutionEffects.DIZZINESS.get()) && entity.getRandom().nextFloat() < 0.05 * (amplifier + 1)) {
            entity.addEffect(new EffectInstance(EvolutionEffects.DIZZINESS.get(), 400 * (amplifier + 1), amplifier, false, false, false));
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        if (amplifier <= 2) {
            return duration % 80 == 0;
        }
        return duration % Math.max(80 >> amplifier - 2, 1) == 0;
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeModifierManager attributes, int amplifier) {
        if (amplifier >= 1) {
            entity.removeEffect(Effects.MOVEMENT_SLOWDOWN);
        }
        super.removeAttributeModifiers(entity, attributes, amplifier);
    }
}
