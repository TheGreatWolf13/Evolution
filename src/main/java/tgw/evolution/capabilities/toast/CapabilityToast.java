package tgw.evolution.capabilities.toast;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;

public final class CapabilityToast {

    public static final Capability<IToastData> INSTANCE = CapabilityManager.get(new CapabilityToken<>() {
    });

    private CapabilityToast() {
    }

    public static void register(RegisterCapabilitiesEvent event) {
        event.register(IToastData.class);
//        CapabilityManager.INSTANCE.register(IToastData.class, new Capability.IStorage<IToastData>() {
//
//            @Override
//            public void readNBT(Capability<IToastData> capability, IToastData handler, Direction side, INBT nbt) {
//                handler.deserializeNBT((CompoundNBT) nbt);
//            }
//
//            @Nullable
//            @Override
//            public INBT writeNBT(Capability<IToastData> capability, IToastData handler, Direction side) {
//                return handler.serializeNBT();
//            }
//
//        }, () -> {
//            throw new IllegalStateException("Could not register CapabilityToast");
//        });
    }
}
