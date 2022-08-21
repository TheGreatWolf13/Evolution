package tgw.evolution.potion;

import net.minecraft.world.effect.MobEffectCategory;

public class EffectThirst extends EffectGeneric {

    public EffectThirst() {
        super(MobEffectCategory.HARMFUL, 0x45_ff4b);
    }

    @Override
    public float thirstMod(int lvl) {
        return 0.1f * (lvl + 1);
    }
}
