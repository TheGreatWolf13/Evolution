package tgw.evolution.potion;

import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraft.potion.Effects;
import tgw.evolution.init.EvolutionDamage;

import java.util.Random;

public class EffectWaterIntoxication extends Effect {

    private static final Random RANDOM = new Random();

    public EffectWaterIntoxication() {
        super(EffectType.HARMFUL, 0x12_6eff);
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        if (amplifier > 0) {
            return duration % 40 == 0;
        }
        return true;
    }

    @Override
    public void performEffect(LivingEntity entity, int amplifier) {
        if (amplifier > 0) {
            entity.attackEntityFrom(EvolutionDamage.WATER_INTOXICATION, amplifier);
            if (!entity.isPotionActive(Effects.NAUSEA) && RANDOM.nextFloat() < 0.15) {
                entity.addPotionEffect(new EffectInstance(Effects.NAUSEA, 400, 1, true, false, false));
            }
            if (!entity.isPotionActive(Effects.WEAKNESS) && RANDOM.nextFloat() < 0.15) {
                entity.addPotionEffect(new EffectInstance(Effects.WEAKNESS, 400, 1, true, false, false));
            }
        }
        else {
            if (!entity.isPotionActive(Effects.NAUSEA) && RANDOM.nextFloat() < 0.001) {
                entity.addPotionEffect(new EffectInstance(Effects.NAUSEA, 200, 0, true, false, false));
            }
            if (!entity.isPotionActive(Effects.WEAKNESS) && RANDOM.nextFloat() < 0.001) {
                entity.addPotionEffect(new EffectInstance(Effects.WEAKNESS, 200, 0, true, false, false));
            }
        }
    }
}
