package tgw.evolution.capabilities;

import net.minecraft.core.Direction;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nullable;

public class SerializableCapabilityProvider<C extends INBTSerializable<N>, N extends Tag> extends SimpleCapabilityProvider<C> implements
                                                                                                                              ICapabilitySerializable<N> {

    /**
     * Create a provider for the specified handler instance.
     *
     * @param capability The Capability instance to provide the handler for
     * @param facing     The Direction to provide the handler for
     * @param instance   The handler instance to provide
     */
    public SerializableCapabilityProvider(Capability<C> capability, @Nullable Direction facing, @Nullable C instance) {
        super(capability, facing, instance);
    }

    public SerializableCapabilityProvider(Capability<C> capability, @Nullable C instance) {
        super(capability, null, instance);
    }

    @Override
    public void deserializeNBT(N nbt) {
        C instance = this.getHandler();
        if (instance == null) {
            return;
        }
        instance.deserializeNBT(nbt);
    }

    @Nullable
    @Override
    public N serializeNBT() {
        C instance = this.getHandler();
        if (instance == null) {
            return null;
        }
        return instance.serializeNBT();
    }
}