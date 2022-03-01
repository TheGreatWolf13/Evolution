package tgw.evolution.capabilities.food;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.util.INBTSerializable;

public interface IHunger extends INBTSerializable<CompoundTag> {

    int getHungerLevel();

    int getSaturationLevel();

    void increaseHungerLevel(int amount);

    void increaseSaturationLevel(int amount);

    void setHungerLevel(int hunger);

    void setSaturationLevel(int saturation);

    void tick(ServerPlayer player);
}
