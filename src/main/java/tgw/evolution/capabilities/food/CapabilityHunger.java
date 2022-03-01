package tgw.evolution.capabilities.food;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;

public final class CapabilityHunger {

    public static final Capability<IHunger> INSTANCE = CapabilityManager.get(new CapabilityToken<>() {
    });

    private CapabilityHunger() {
    }

    public static void register(RegisterCapabilitiesEvent event) {
        event.register(IHunger.class);
//        CapabilityManager.INSTANCE.register(IHunger.class, new Capability.IStorage<IHunger>() {
//
//            @Override
//            public void readNBT(Capability<IHunger> capability, IHunger handler, Direction side, INBT nbt) {
//                handler.deserializeNBT((CompoundNBT) nbt);
//            }
//
//            @Nullable
//            @Override
//            public INBT writeNBT(Capability<IHunger> capability, IHunger handler, Direction side) {
//                return handler.serializeNBT();
//            }
//
//        }, () -> {
//            throw new IllegalStateException("Could not register CapabilityHunger");
//        });
    }
}
