package tgw.evolution.init;

import tgw.evolution.capabilities.chunkstorage.CapabilityChunkStorage;
import tgw.evolution.capabilities.health.CapabilityHealth;
import tgw.evolution.capabilities.inventory.CapabilityExtendedInventory;
import tgw.evolution.capabilities.thirst.CapabilityThirst;
import tgw.evolution.capabilities.toast.CapabilityToast;

public final class EvolutionCapabilities {

    private EvolutionCapabilities() {
    }

    public static void register() {
        CapabilityChunkStorage.register();
        CapabilityExtendedInventory.register();
        CapabilityThirst.register();
        CapabilityHealth.register();
        CapabilityToast.register();
    }
}
