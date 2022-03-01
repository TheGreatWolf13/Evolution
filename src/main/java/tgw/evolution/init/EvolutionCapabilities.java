package tgw.evolution.init;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.INBTSerializable;
import tgw.evolution.Evolution;
import tgw.evolution.capabilities.chunkstorage.CapabilityChunkStorage;
import tgw.evolution.capabilities.food.CapabilityHunger;
import tgw.evolution.capabilities.health.CapabilityHealth;
import tgw.evolution.capabilities.inventory.CapabilityExtendedInventory;
import tgw.evolution.capabilities.modular.CapabilityModular;
import tgw.evolution.capabilities.temperature.CapabilityTemperature;
import tgw.evolution.capabilities.thirst.CapabilityThirst;
import tgw.evolution.capabilities.toast.CapabilityToast;

public final class EvolutionCapabilities {

    private EvolutionCapabilities() {
    }

    public static <T extends INBTSerializable<CompoundTag>> void clonePlayer(Player oldPlayer, Player newPlayer, Capability<T> cap) {
        try {
            oldPlayer.reviveCaps();
            T oldCap = oldPlayer.getCapability(cap).orElseThrow(IllegalStateException::new);
            oldPlayer.invalidateCaps();
            T newCap = newPlayer.getCapability(cap).orElseThrow(IllegalStateException::new);
            newCap.deserializeNBT(oldCap.serializeNBT());
        }
        catch (Exception e) {
            Evolution.error("Could not clone {} for {}: ", cap.getName(), oldPlayer.getScoreboardName(), e);
        }
    }

    public static void register(RegisterCapabilitiesEvent event) {
        CapabilityChunkStorage.register(event);
        CapabilityExtendedInventory.register(event);
        CapabilityThirst.register(event);
        CapabilityHealth.register(event);
        CapabilityToast.register(event);
        CapabilityHunger.register(event);
        CapabilityTemperature.register(event);
        CapabilityModular.register(event);
    }
}
