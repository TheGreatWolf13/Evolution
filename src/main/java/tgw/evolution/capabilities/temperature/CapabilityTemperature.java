package tgw.evolution.capabilities.temperature;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;

public final class CapabilityTemperature {

    public static final Capability<ITemperature> INSTANCE = CapabilityManager.get(new CapabilityToken<>() {
    });

    private CapabilityTemperature() {
    }

    public static void register(RegisterCapabilitiesEvent event) {
        event.register(ITemperature.class);
//        CapabilityManager.INSTANCE.register(ITemperature.class, new Capability.IStorage<ITemperature>() {
//
//            @Override
//            public void readNBT(Capability<ITemperature> capability, ITemperature handler, Direction side, INBT nbt) {
//                handler.deserializeNBT((CompoundNBT) nbt);
//            }
//
//            @Nullable
//            @Override
//            public INBT writeNBT(Capability<ITemperature> capability, ITemperature handler, Direction side) {
//                return handler.serializeNBT();
//            }
//
//        }, () -> {
//            throw new IllegalStateException("Could not register CapabilityTemperature");
//        });
    }
}
