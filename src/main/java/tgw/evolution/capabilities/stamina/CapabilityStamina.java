package tgw.evolution.capabilities.stamina;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;

public final class CapabilityStamina {

    public static final Capability<IStamina> INSTANCE = CapabilityManager.get(new CapabilityToken<>() {
    });

    private CapabilityStamina() {
    }

    public static void register(RegisterCapabilitiesEvent event) {
        event.register(IStamina.class);
    }
}
