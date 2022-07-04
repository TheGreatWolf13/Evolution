package tgw.evolution.capabilities.stamina;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

public class StaminaStats implements IStamina {

    public static final int MAX_STAMINA = 0;

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        //TODO implementation

    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        //TODO implementation
        return nbt;
    }

    @Override
    public void setStamina(int stamina) {
        //TODO implementation

    }

    @Override
    public void tick(ServerPlayer player) {
        //TODO implementation

    }
}
