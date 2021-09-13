package tgw.evolution.potion;

import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraft.potion.Effects;
import tgw.evolution.init.EvolutionDamage;

import java.util.Random;

public class EffectWaterIntoxication extends Effect {

    public EffectWaterIntoxication() {
        super(EffectType.HARMFUL, 0x12_6eff);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        Random random = entity.getRandom();
        if (amplifier > 0) {
            entity.hurt(EvolutionDamage.WATER_INTOXICATION, amplifier);
            if (!entity.hasEffect(Effects.CONFUSION) && random.nextFloat() < 0.15 * amplifier) {
                entity.addEffect(new EffectInstance(Effects.CONFUSION, 400 * amplifier, amplifier, true, false, false));
            }
            if (!entity.hasEffect(Effects.WEAKNESS) && random.nextFloat() < 0.15 * amplifier) {
                entity.addEffect(new EffectInstance(Effects.WEAKNESS, 400 * amplifier, amplifier, true, false, false));
            }
        }
        else {
            if (!entity.hasEffect(Effects.CONFUSION) && random.nextFloat() < 0.001) {
                entity.addEffect(new EffectInstance(Effects.CONFUSION, 200, 0, true, false, false));
            }
            if (!entity.hasEffect(Effects.WEAKNESS) && random.nextFloat() < 0.001) {
                entity.addEffect(new EffectInstance(Effects.WEAKNESS, 200, 0, true, false, false));
            }
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        if (amplifier > 0) {
            return duration % 40 == 0;
        }
        return true;
    }
}
