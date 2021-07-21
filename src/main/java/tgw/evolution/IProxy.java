package tgw.evolution;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import tgw.evolution.util.SkinType;

public interface IProxy {

    PlayerEntity getClientPlayer();

    World getClientWorld();

    SkinType getSkinType();

    void init();
}
