package tgw.evolution.capabilities.stamina;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import tgw.evolution.Evolution;

public final class CapabilityStamina {

    public static final Capability<IStamina> INSTANCE = CapabilityManager.get(new CapabilityToken<>() {
    });
    public static final ResourceLocation LOC = Evolution.getResource("stamina");

    private CapabilityStamina() {
    }

    public static void register(RegisterCapabilitiesEvent event) {
        event.register(IStamina.class);
    }
}
