package tgw.evolution.patches;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

public interface IMobEffectInstancePatch {

    float getAbsoluteDuration();

    MobEffectInstance getHiddenEffect();

    boolean isInfinite();

    boolean isSpashPatch();

    void setHiddenEffect(MobEffectInstance hiddenInstance);

    void setInfinite(boolean infinite);

    @SuppressWarnings("UnusedReturnValue")
    int tickDownDurationPatch();

    boolean updateWithEntity(MobEffectInstance other, LivingEntity entity);
}
