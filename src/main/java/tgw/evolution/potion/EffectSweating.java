package tgw.evolution.potion;

import net.minecraft.world.effect.MobEffectCategory;

public class EffectSweating extends EffectGeneric {

    public EffectSweating() {
        super(MobEffectCategory.NEUTRAL, 0x0067_dd);
    }

    @Override
    public double tempMod() {
        return -2;
    }

    @Override
    public float thirstMod(int lvl) {
        return 0.15f;
    }
}
