package tgw.evolution.capabilities.health;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.util.INBTSerializable;

public interface IHealth extends INBTSerializable<CompoundTag> {

    /**
     * Called every tick to tick the player's HealthStats
     *
     * @param player The Player being ticked
     */
    void tick(ServerPlayer player);
}
