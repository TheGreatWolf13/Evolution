package tgw.evolution.capabilities;

import net.minecraft.nbt.Tag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.INBTSerializable;

import org.jetbrains.annotations.Nullable;

public class SerializableCapabilityProvider<C extends INBTSerializable<N>, N extends Tag> extends SimpleCapabilityProvider<C>
        implements ICapabilitySerializable<N> {

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