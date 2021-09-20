package tgw.evolution.capabilities.health;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import tgw.evolution.util.InjectionUtil;

import javax.annotation.Nullable;

public final class CapabilityHealth {

    @CapabilityInject(IHealth.class)
    public static final Capability<IHealth> INSTANCE = InjectionUtil.Null();

    private CapabilityHealth() {
    }

    public static void register() {
        CapabilityManager.INSTANCE.register(IHealth.class, new Capability.IStorage<IHealth>() {

            @Override
            public void readNBT(Capability<IHealth> capability, IHealth handler, Direction side, INBT nbt) {
                handler.deserializeNBT((CompoundNBT) nbt);
            }

            @Nullable
            @Override
            public INBT writeNBT(Capability<IHealth> capability, IHealth handler, Direction side) {
                return handler.serializeNBT();
            }

        }, () -> {
            throw new IllegalStateException("Could not register CapabilityHealth");
        });
    }
}
