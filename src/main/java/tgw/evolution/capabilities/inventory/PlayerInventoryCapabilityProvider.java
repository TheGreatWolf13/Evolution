package tgw.evolution.capabilities.inventory;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import tgw.evolution.inventory.ContainerExtendedHandler;

public class PlayerInventoryCapabilityProvider implements INBTSerializable<CompoundNBT>, ICapabilityProvider {

    private final LazyOptional<ContainerExtendedHandler> handler = LazyOptional.of(this::createHandler);

    private ContainerExtendedHandler createHandler() {
        return new ContainerExtendedHandler();
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        this.handler.orElse(null).deserializeNBT(nbt);
    }

    @Override
    public CompoundNBT serializeNBT() {
        return this.handler.orElse(null).serializeNBT();
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction facing) {
        if (capability == PlayerInventoryCapability.CAPABILITY_EXTENDED_INVENTORY) {
            return this.handler.cast();
        }
        throw new IllegalStateException("Wrong capability: " + capability);
    }
}
