package tgw.evolution.capabilities.stamina;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.util.INBTSerializable;

public interface IStamina extends INBTSerializable<CompoundTag> {

    void setStamina(int stamina);

    void tick(ServerPlayer player);
}
