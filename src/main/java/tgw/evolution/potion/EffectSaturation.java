package tgw.evolution.potion;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import tgw.evolution.capabilities.food.CapabilityHunger;
import tgw.evolution.capabilities.food.IHunger;
import tgw.evolution.init.EvolutionCapabilities;

public class EffectSaturation extends EffectGeneric {

    public EffectSaturation() {
        super(MobEffectCategory.BENEFICIAL, 0xf8_2423);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity instanceof ServerPlayer player) {
            IHunger hunger = EvolutionCapabilities.getRevivedCapability(player, CapabilityHunger.INSTANCE);
            hunger.increaseHungerLevel(1 + amplifier);
        }
    }

    @Override
    public int tickInterval(int lvl) {
        return 1;
    }
}
