package tgw.evolution.potion;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import tgw.evolution.capabilities.food.CapabilityHunger;
import tgw.evolution.capabilities.food.IHunger;

public class EffectSaturation extends MobEffect {

    public EffectSaturation() {
        super(MobEffectCategory.BENEFICIAL, 0xf8_2423);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity instanceof ServerPlayer player) {
            if (!player.isAlive()) {
                player.reviveCaps();
            }
            IHunger hunger = player.getCapability(CapabilityHunger.INSTANCE).orElseThrow(IllegalStateException::new);
            hunger.increaseHungerLevel(1 + amplifier);
            if (!player.isAlive()) {
                player.invalidateCaps();
            }
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}
