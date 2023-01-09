package tgw.evolution.capabilities.health;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import tgw.evolution.Evolution;

public final class CapabilityHealth {

    public static final Capability<IHealth> INSTANCE = CapabilityManager.get(new CapabilityToken<>() {
    });
    public static final ResourceLocation LOC = Evolution.getResource("health");

    private CapabilityHealth() {
    }

    public static void register(RegisterCapabilitiesEvent event) {
        event.register(IHealth.class);
    }
}
