package tgw.evolution.capabilities.health;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;

public interface IHealth extends INBTSerializable<CompoundNBT> {

    /**
     * Called every tick to tick the player's HealthStats
     *
     * @param player The Player being ticked
     */
    void tick(ServerPlayerEntity player);
}
