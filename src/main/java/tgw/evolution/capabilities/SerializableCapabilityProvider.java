package tgw.evolution.capabilities;

import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nullable;

public class SerializableCapabilityProvider<HANDLER> extends SimpleCapabilityProvider<HANDLER> implements INBTSerializable<INBT> {

    /**
     * Create a provider for the specified handler instance.
     *
     * @param capability The Capability instance to provide the handler for
     * @param facing     The Direction to provide the handler for
     * @param instance   The handler instance to provide
     */
    public SerializableCapabilityProvider(Capability<HANDLER> capability, @Nullable Direction facing, @Nullable HANDLER instance) {
        super(capability, facing, instance);
    }

    public SerializableCapabilityProvider(Capability<HANDLER> capability, @Nullable HANDLER instance) {
        super(capability, null, instance);
    }

    @Override
    public void deserializeNBT(INBT nbt) {
        HANDLER instance = this.getHandler();
        if (instance == null) {
            return;
        }
        this.getCapability().readNBT(instance, this.getFacing(), nbt);
    }

    @Nullable
    @Override
    public INBT serializeNBT() {
        HANDLER instance = this.getHandler();
        if (instance == null) {
            return null;
        }
        return this.getCapability().writeNBT(instance, this.getFacing());
    }
}