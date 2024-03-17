package tgw.evolution.patches;

import net.minecraft.world.level.ChunkPos;
import tgw.evolution.capabilities.player.*;
import tgw.evolution.util.OptionalMutableChunkPos;

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

    default OptionalMutableChunkPos getLastCameraChunkPos() {
        throw new AbstractMethodError();
    }

    default ChunkPos getLastChunkPos() {
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

    default void setLastChunkPos_(int secX, int secZ) {
        throw new AbstractMethodError();
    }
}
