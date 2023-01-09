package tgw.evolution.capabilities.inventory;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import tgw.evolution.Evolution;

public final class CapabilityInventory {

    public static final Capability<IInventory> INSTANCE = CapabilityManager.get(new CapabilityToken<>() {
    });
    public static final ResourceLocation LOC = Evolution.getResource("extended_inventory");

    private CapabilityInventory() {
    }

    public static void register(RegisterCapabilitiesEvent event) {
        event.register(IInventory.class);
    }
}
