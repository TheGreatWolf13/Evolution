package tgw.evolution.potion;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import tgw.evolution.capabilities.thirst.CapabilityThirst;
import tgw.evolution.capabilities.thirst.IThirst;
import tgw.evolution.init.EvolutionCapabilities;

public class EffectHydration extends EffectGeneric {

    public EffectHydration() {
        super(MobEffectCategory.BENEFICIAL, 0x14_28ff);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int lvl) {
        if (entity instanceof ServerPlayer player) {
            IThirst thirst = EvolutionCapabilities.getRevivedCapability(player, CapabilityThirst.INSTANCE);
            thirst.increaseThirstLevel(1 + lvl);
        }
    }

    @Override
    public int tickInterval(int lvl) {
        return 1;
    }
}
