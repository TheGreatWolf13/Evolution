package tgw.evolution.init;

import tgw.evolution.capabilities.chunkstorage.CapabilityChunkStorage;
import tgw.evolution.capabilities.inventory.CapabilityExtendedInventory;
import tgw.evolution.capabilities.thirst.CapabilityThirst;

public final class EvolutionCapabilities {

    private EvolutionCapabilities() {
    }

    public static void register() {
        CapabilityChunkStorage.register();
        CapabilityExtendedInventory.register();
        CapabilityThirst.register();
    }
}
