package tgw.evolution.potion;

import net.minecraft.world.effect.MobEffectCategory;

public class EffectShivering extends EffectGeneric {

    public EffectShivering() {
        super(MobEffectCategory.NEUTRAL, 0xee_eeee);
    }

    @Override
    public float hungerMod(int lvl) {
        return 0.15f;
    }

    @Override
    public double tempMod() {
        return 2;
    }
}
