package tgw.evolution.patches;

import net.minecraft.core.SectionPos;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.capabilities.player.*;

public interface PatchServerPlayer extends PatchPlayer {

    boolean getCameraUnload();

    CapabilityInventory getExtraInventory();

    CapabilityHealth getHealthStats();

    CapabilityHunger getHungerStats();

    @Nullable SectionPos getLastCameraSectionPos();

    CapabilityStamina getStaminaStats();

    CapabilityTemperature getTemperatureStats();

    CapabilityThirst getThirstStats();

    CapabilityToast getToastStats();

    void setCameraUnload(boolean shouldUnload);

    void setLastCameraSectionPos(@Nullable SectionPos pos);
}
