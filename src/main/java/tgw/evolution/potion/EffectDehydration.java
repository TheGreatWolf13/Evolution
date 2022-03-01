package tgw.evolution.potion;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.init.EvolutionEffects;
import tgw.evolution.patches.IEffectInstancePatch;

public class EffectDehydration extends MobEffect {

    public EffectDehydration() {
        super(MobEffectCategory.HARMFUL, 0);
    }

    @Override
    public void addAttributeModifiers(LivingEntity entity, AttributeMap attributes, int amplifier) {
        if (amplifier >= 1) {
            entity.addEffect(IEffectInstancePatch.newInfinite(MobEffects.MOVEMENT_SLOWDOWN, amplifier - 1, false, false, false));
        }
        super.addAttributeModifiers(entity, attributes, amplifier);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (amplifier >= 2) {
            entity.hurt(EvolutionDamage.DEHYDRATION, 1.0f);
        }
        if (amplifier >= 1 && !entity.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)) {
            entity.addEffect(IEffectInstancePatch.newInfinite(MobEffects.MOVEMENT_SLOWDOWN, amplifier - 1, false, false, false));
        }
        if (!entity.hasEffect(EvolutionEffects.DIZZINESS.get()) && entity.getRandom().nextFloat() < 0.05 * (amplifier + 1)) {
            entity.addEffect(new MobEffectInstance(EvolutionEffects.DIZZINESS.get(), 400 * (amplifier + 1), amplifier, false, false, false));
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
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributes, int amplifier) {
        if (amplifier >= 1) {
            entity.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
        }
        super.removeAttributeModifiers(entity, attributes, amplifier);
    }
}
