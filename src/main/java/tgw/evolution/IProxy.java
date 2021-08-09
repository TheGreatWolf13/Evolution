package tgw.evolution;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.stats.Stat;
import net.minecraft.world.World;
import tgw.evolution.util.SkinType;

public interface IProxy {

    PlayerEntity getClientPlayer();

    World getClientWorld();

    SkinType getSkinType();

    void init();

    void updateStats(Object2LongMap<Stat<?>> statsData);
}
