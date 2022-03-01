package tgw.evolution.potion;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import tgw.evolution.init.EvolutionDamage;

import java.util.Random;

public class EffectWaterIntoxication extends MobEffect {

    public EffectWaterIntoxication() {
        super(MobEffectCategory.HARMFUL, 0x12_6eff);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        Random random = entity.getRandom();
        if (amplifier > 0) {
            entity.hurt(EvolutionDamage.WATER_INTOXICATION, amplifier);
            if (!entity.hasEffect(MobEffects.CONFUSION) && random.nextFloat() < 0.15 * amplifier) {
                entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 400 * amplifier, 0, true, false, false));
            }
            if (!entity.hasEffect(MobEffects.WEAKNESS) && random.nextFloat() < 0.15 * amplifier) {
                entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 400 * amplifier, amplifier, true, false, false));
            }
        }
        else {
            if (!entity.hasEffect(MobEffects.CONFUSION) && random.nextFloat() < 0.001) {
                entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 0, true, false, false));
            }
            if (!entity.hasEffect(MobEffects.WEAKNESS) && random.nextFloat() < 0.001) {
                entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 0, true, false, false));
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
