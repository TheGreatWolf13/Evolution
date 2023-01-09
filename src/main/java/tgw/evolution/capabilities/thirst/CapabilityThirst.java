package tgw.evolution.capabilities.thirst;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import tgw.evolution.Evolution;

public final class CapabilityThirst {

    public static final Capability<IThirst> INSTANCE = CapabilityManager.get(new CapabilityToken<>() {
    });
    public static final ResourceLocation LOC = Evolution.getResource("thirst");

    private CapabilityThirst() {
    }

    public static void register(RegisterCapabilitiesEvent event) {
        event.register(IThirst.class);
    }
}
