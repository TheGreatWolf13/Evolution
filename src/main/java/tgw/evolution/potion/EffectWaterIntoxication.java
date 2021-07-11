package tgw.evolution.potion;

import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;
import tgw.evolution.init.EvolutionDamage;

public class EffectWaterIntoxication extends Effect {

    public EffectWaterIntoxication() {
        super(EffectType.HARMFUL, 0x12_6eff);
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        if (amplifier > 0) {
            return duration % 40 == 0;
        }
        return false;
    }

    @Override
    public void performEffect(LivingEntity entity, int amplifier) {
        if (amplifier > 0) {
            entity.attackEntityFrom(EvolutionDamage.WATER_INTOXICATION, amplifier);
        }
    }
}
