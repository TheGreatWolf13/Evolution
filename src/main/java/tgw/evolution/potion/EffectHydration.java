package tgw.evolution.potion;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import tgw.evolution.capabilities.thirst.CapabilityThirst;
import tgw.evolution.capabilities.thirst.IThirst;

public class EffectHydration extends MobEffect {

    public EffectHydration() {
        super(MobEffectCategory.BENEFICIAL, 0x14_28ff);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity instanceof ServerPlayer player) {
            if (!player.isAlive()) {
                player.reviveCaps();
            }
            IThirst thirst = player.getCapability(CapabilityThirst.INSTANCE).orElseThrow(IllegalStateException::new);
            thirst.increaseThirstLevel(1 + amplifier);
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
