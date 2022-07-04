package tgw.evolution.capabilities.temperature;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;

public final class CapabilityTemperature {

    public static final Capability<ITemperature> INSTANCE = CapabilityManager.get(new CapabilityToken<>() {
    });

    private CapabilityTemperature() {
    }

    public static void register(RegisterCapabilitiesEvent event) {
        event.register(ITemperature.class);
    }
}
