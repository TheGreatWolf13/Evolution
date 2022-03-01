package tgw.evolution.patches;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

public interface IEffectInstancePatch {

    static MobEffectInstance newInfinite(MobEffect effect, int amplifier, boolean isAmbient, boolean showParticles, boolean showIcon) {
        MobEffectInstance instance = new MobEffectInstance(effect, 10, amplifier, isAmbient, showParticles, showIcon);
        ((IEffectInstancePatch) instance).setInfinite(true);
        return instance;
    }

    int getAbsoluteDuration();

    MobEffectInstance getHiddenEffect();

    boolean isInfinite();

    boolean isSpashPatch();

    void setHiddenEffect(MobEffectInstance hiddenInstance);

    void setInfinite(boolean infinite);

    @SuppressWarnings("UnusedReturnValue")
    int tickDownDurationPatch();
}
