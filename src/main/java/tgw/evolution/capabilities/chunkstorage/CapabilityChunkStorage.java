package tgw.evolution.capabilities.chunkstorage;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import tgw.evolution.Evolution;

public final class CapabilityChunkStorage {

    public static final Capability<IChunkStorage> INSTANCE = CapabilityManager.get(new CapabilityToken<>() {});
    public static final ResourceLocation LOC = Evolution.getResource("storage");

    private CapabilityChunkStorage() {
    }

    public static void register(RegisterCapabilitiesEvent event) {
        event.register(IChunkStorage.class);
    }
}
