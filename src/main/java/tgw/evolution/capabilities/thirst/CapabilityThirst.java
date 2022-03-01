package tgw.evolution.capabilities.thirst;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;

public final class CapabilityThirst {

    public static final Capability<IThirst> INSTANCE = CapabilityManager.get(new CapabilityToken<>() {
    });

    private CapabilityThirst() {
    }

    public static void register(RegisterCapabilitiesEvent event) {
        event.register(IThirst.class);
//        CapabilityManager.INSTANCE.register(IThirst.class, new Capability.IStorage<IThirst>() {
//
//            @Override
//            public void readNBT(Capability<IThirst> capability, IThirst handler, Direction side, INBT nbt) {
//                handler.deserializeNBT((CompoundNBT) nbt);
//            }
//
//            @Nullable
//            @Override
//            public INBT writeNBT(Capability<IThirst> capability, IThirst handler, Direction side) {
//                return handler.serializeNBT();
//            }
//
//        }, () -> {
//            throw new IllegalStateException("Could not register CapabilityThirst");
//        });
    }
}
