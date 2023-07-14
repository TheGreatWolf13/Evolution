package tgw.evolution.patches;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public interface PatchMobEffectInstance {

    default float getAbsoluteDuration() {
        throw new AbstractMethodError();
    }

    default @Nullable MobEffectInstance getHiddenEffect() {
        throw new AbstractMethodError();
    }

    default boolean isInfinite() {
        throw new AbstractMethodError();
    }

    default void setHiddenEffect(@Nullable MobEffectInstance hiddenInstance) {
        throw new AbstractMethodError();
    }

    default void setInfinite(boolean infinite) {
        throw new AbstractMethodError();
    }

    @SuppressWarnings("UnusedReturnValue")
    default int tickDownDurationPatch() {
        throw new AbstractMethodError();
    }

    default boolean updateWithEntity(MobEffectInstance other, LivingEntity entity) {
        throw new AbstractMethodError();
    }
}
