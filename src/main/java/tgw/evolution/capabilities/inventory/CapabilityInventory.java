package tgw.evolution.capabilities.inventory;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;

public final class CapabilityInventory {

    public static final Capability<IInventory> INSTANCE = CapabilityManager.get(new CapabilityToken<>() {
    });

    private CapabilityInventory() {
    }

    public static void register(RegisterCapabilitiesEvent event) {
        event.register(IInventory.class);
    }
}
