package tgw.evolution.capabilities.thirst;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import tgw.evolution.util.InjectionUtil;

import javax.annotation.Nullable;

public final class CapabilityThirst {

    @CapabilityInject(IThirst.class)
    public static final Capability<IThirst> INSTANCE = InjectionUtil.Null();

    private CapabilityThirst() {
    }

    public static void register() {
        CapabilityManager.INSTANCE.register(IThirst.class, new Capability.IStorage<IThirst>() {

            @Override
            public void readNBT(Capability<IThirst> capability, IThirst handler, Direction side, INBT nbt) {
                handler.deserializeNBT((CompoundNBT) nbt);
            }

            @Nullable
            @Override
            public INBT writeNBT(Capability<IThirst> capability, IThirst handler, Direction side) {
                return handler.serializeNBT();
            }

        }, () -> {
            throw new IllegalStateException("Could not register CapabilityThirst");
        });
    }
}
