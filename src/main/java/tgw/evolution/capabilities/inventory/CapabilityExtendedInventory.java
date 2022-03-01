package tgw.evolution.capabilities.inventory;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import tgw.evolution.inventory.extendedinventory.IExtendedInventory;

public final class CapabilityExtendedInventory {

    public static final Capability<IExtendedInventory> INSTANCE = CapabilityManager.get(new CapabilityToken<>() {
    });

    private CapabilityExtendedInventory() {
    }

    public static void register(RegisterCapabilitiesEvent event) {
        event.register(IExtendedInventory.class);
//        CapabilityManager.INSTANCE.register(IExtendedInventory.class, new Capability.IStorage<IExtendedInventory>() {
//
//            @Override
//            public void readNBT(Capability<IExtendedInventory> capability, IExtendedInventory handler, Direction facing, INBT nbt) {
//                handler.deserializeNBT((CompoundNBT) nbt);
//            }
//
//            @Override
//            public INBT writeNBT(Capability<IExtendedInventory> capability, IExtendedInventory handler, Direction facing) {
//                return handler.serializeNBT();
//            }
//
//        }, () -> {
//            throw new IllegalStateException("Could not register CapabilityExtendedInventory");
//        });
    }
}
