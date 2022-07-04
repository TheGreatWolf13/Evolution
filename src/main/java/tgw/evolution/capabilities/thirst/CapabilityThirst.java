package tgw.evolution.capabilities.thirst;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;

public final class CapabilityThirst {

    public static final Capability<IThirst> INSTANCE = CapabilityManager.get(new CapabilityToken<>() {
    });

    private CapabilityThirst() {
    }

    public static void register(RegisterCapabilitiesEvent event) {
        event.register(IThirst.class);
    }
}
