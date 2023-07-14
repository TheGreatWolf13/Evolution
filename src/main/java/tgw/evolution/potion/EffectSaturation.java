package tgw.evolution.potion;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import tgw.evolution.capabilities.player.CapabilityHunger;
import tgw.evolution.patches.PatchServerPlayer;

public class EffectSaturation extends EffectGeneric {

    public EffectSaturation() {
        super(MobEffectCategory.BENEFICIAL, 0xf8_2423);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity instanceof PatchServerPlayer player) {
            CapabilityHunger hunger = player.getHungerStats();
            hunger.increaseHungerLevel(1 + amplifier);
        }
    }

    @Override
    public int tickInterval(int lvl) {
        return 1;
    }
}
