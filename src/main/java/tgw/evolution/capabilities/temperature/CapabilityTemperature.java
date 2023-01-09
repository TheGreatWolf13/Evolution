package tgw.evolution.capabilities.temperature;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import tgw.evolution.Evolution;

public final class CapabilityTemperature {

    public static final Capability<ITemperature> INSTANCE = CapabilityManager.get(new CapabilityToken<>() {
    });
    public static final ResourceLocation LOC = Evolution.getResource("temperature");

    private CapabilityTemperature() {
    }

    public static void register(RegisterCapabilitiesEvent event) {
        event.register(ITemperature.class);
    }
}
