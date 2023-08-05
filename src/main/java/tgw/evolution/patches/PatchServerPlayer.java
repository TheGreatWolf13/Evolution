package tgw.evolution.patches;

import net.minecraft.core.SectionPos;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.capabilities.player.*;

public interface PatchServerPlayer {

    default boolean getCameraUnload() {
        throw new AbstractMethodError();
    }

    default CapabilityInventory getExtraInventory() {
        throw new AbstractMethodError();
    }

    default CapabilityHealth getHealthStats() {
        throw new AbstractMethodError();
    }

    default CapabilityHunger getHungerStats() {
        throw new AbstractMethodError();
    }

    default @Nullable SectionPos getLastCameraSectionPos() {
        throw new AbstractMethodError();
    }

    default CapabilityStamina getStaminaStats() {
        throw new AbstractMethodError();
    }

    default CapabilityTemperature getTemperatureStats() {
        throw new AbstractMethodError();
    }

    default CapabilityThirst getThirstStats() {
        throw new AbstractMethodError();
    }

    default CapabilityToast getToastStats() {
        throw new AbstractMethodError();
    }

    default void setCameraUnload(boolean shouldUnload) {
        throw new AbstractMethodError();
    }

    default void setLastCameraSectionPos(@Nullable SectionPos pos) {
        throw new AbstractMethodError();
    }
}
