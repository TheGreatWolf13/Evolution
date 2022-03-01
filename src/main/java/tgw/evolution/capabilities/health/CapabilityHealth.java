package tgw.evolution.capabilities.health;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;

public final class CapabilityHealth {

    public static final Capability<IHealth> INSTANCE = CapabilityManager.get(new CapabilityToken<>() {
    });

    private CapabilityHealth() {
    }

    public static void register(RegisterCapabilitiesEvent event) {
        event.register(IHealth.class);
    }
}
