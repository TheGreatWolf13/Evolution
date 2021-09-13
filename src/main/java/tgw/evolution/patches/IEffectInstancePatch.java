package tgw.evolution.patches;

import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;

public interface IEffectInstancePatch {

    static EffectInstance newInfinite(Effect effect, int amplifier, boolean isAmbient, boolean showParticles, boolean showIcon) {
        EffectInstance instance = new EffectInstance(effect, 10, amplifier, isAmbient, showParticles, showIcon);
        ((IEffectInstancePatch) instance).setInfinite(true);
        return instance;
    }

    int getAbsoluteDuration();

    EffectInstance getHiddenEffect();

    boolean isInfinite();

    boolean isSpashPatch();

    void setHiddenEffect(EffectInstance hiddenInstance);

    void setInfinite(boolean infinite);

    @SuppressWarnings("UnusedReturnValue")
    int tickDownDurationPatch();
}
