package tgw.evolution.capabilities.temperature;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.util.INBTSerializable;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.util.earth.ClimateZone;

public interface ITemperature extends INBTSerializable<CompoundTag> {

    int getCurrentMaxComfort();

    int getCurrentMinComfort();

    int getCurrentTemperature();

    int getDesiredMaxComfort();

    int getDesiredMinComfort();

    @Nullable ClimateZone.Region getRegion();

    void setCurrentMaxComfort(double temp);

    void setCurrentMinComfort(double temp);

    void setCurrentTemperature(double temp);

    void setDesiredTemperature(double temp);

    void tick(ServerPlayer player);
}
