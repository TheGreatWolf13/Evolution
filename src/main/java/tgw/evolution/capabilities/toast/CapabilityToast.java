package tgw.evolution.capabilities.toast;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;

public final class CapabilityToast {

    public static final Capability<IToastData> INSTANCE = CapabilityManager.get(new CapabilityToken<>() {
    });

    private CapabilityToast() {
    }

    public static void register(RegisterCapabilitiesEvent event) {
        event.register(IToastData.class);
    }
}
