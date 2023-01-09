package tgw.evolution.capabilities.toast;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import tgw.evolution.Evolution;

public final class CapabilityToast {

    public static final Capability<IToastData> INSTANCE = CapabilityManager.get(new CapabilityToken<>() {
    });
    public static final ResourceLocation LOC = Evolution.getResource("toast");

    private CapabilityToast() {
    }

    public static void register(RegisterCapabilitiesEvent event) {
        event.register(IToastData.class);
    }
}
