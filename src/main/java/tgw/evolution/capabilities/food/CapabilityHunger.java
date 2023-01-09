package tgw.evolution.capabilities.food;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import tgw.evolution.Evolution;

public final class CapabilityHunger {

    public static final Capability<IHunger> INSTANCE = CapabilityManager.get(new CapabilityToken<>() {
    });
    public static final ResourceLocation LOC = Evolution.getResource("hunger");

    private CapabilityHunger() {
    }

    public static void register(RegisterCapabilitiesEvent event) {
        event.register(IHunger.class);
    }
}
