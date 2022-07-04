package tgw.evolution.capabilities.inventory;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import tgw.evolution.inventory.extendedinventory.IExtendedInventory;

public final class CapabilityExtendedInventory {

    public static final Capability<IExtendedInventory> INSTANCE = CapabilityManager.get(new CapabilityToken<>() {
    });

    private CapabilityExtendedInventory() {
    }

    public static void register(RegisterCapabilitiesEvent event) {
        event.register(IExtendedInventory.class);
    }
}
