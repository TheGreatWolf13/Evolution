package tgw.evolution.capabilities.modular;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import tgw.evolution.capabilities.modular.part.IPart;

public final class CapabilityModular {

    public static final Capability<IModularTool> TOOL = CapabilityManager.get(new CapabilityToken<>() {
    });
    public static final Capability<IPart> PART = CapabilityManager.get(new CapabilityToken<>() {
    });

    private CapabilityModular() {
    }

    public static void register(RegisterCapabilitiesEvent event) {
        event.register(IModularTool.class);
        event.register(IPart.class);
    }
}
