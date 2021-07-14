package tgw.evolution.capabilities.inventory;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import tgw.evolution.inventory.extendedinventory.IExtendedItemHandler;
import tgw.evolution.util.InjectionUtil;

public final class CapabilityExtendedInventory {

    @CapabilityInject(IExtendedItemHandler.class)
    public static final Capability<IExtendedItemHandler> INSTANCE = InjectionUtil.Null();

    private CapabilityExtendedInventory() {
    }

    public static void register() {
        CapabilityManager.INSTANCE.register(IExtendedItemHandler.class, new Capability.IStorage<IExtendedItemHandler>() {

            @Override
            public void readNBT(Capability<IExtendedItemHandler> capability, IExtendedItemHandler handler, Direction facing, INBT nbt) {
                handler.deserializeNBT((CompoundNBT) nbt);
            }

            @Override
            public INBT writeNBT(Capability<IExtendedItemHandler> capability, IExtendedItemHandler handler, Direction facing) {
                return handler.serializeNBT();
            }

        }, () -> {
            throw new IllegalStateException("Could not register CapabilityExtendedInventory");
        });
    }
}
