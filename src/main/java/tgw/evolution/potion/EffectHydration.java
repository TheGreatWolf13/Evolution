package tgw.evolution.potion;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import tgw.evolution.capabilities.player.CapabilityThirst;
import tgw.evolution.patches.PatchServerPlayer;

public class EffectHydration extends EffectGeneric {

    public EffectHydration() {
        super(MobEffectCategory.BENEFICIAL, 0x14_28ff);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int lvl) {
        if (entity instanceof PatchServerPlayer player) {
            CapabilityThirst thirst = player.getThirstStats();
            thirst.increaseThirstLevel(1 + lvl);
        }
    }

    @Override
    public int tickInterval(int lvl) {
        return 1;
    }
}
