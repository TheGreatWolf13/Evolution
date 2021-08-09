package tgw.evolution;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.stats.Stat;
import net.minecraft.world.World;
import tgw.evolution.blocks.tileentities.KnappingPatterns;
import tgw.evolution.blocks.tileentities.MoldingPatterns;
import tgw.evolution.util.SkinType;

public class ServerProxy implements IProxy {

    @Override
    public PlayerEntity getClientPlayer() {
        throw new IllegalStateException("Only run this on the client!");
    }

    @Override
    public World getClientWorld() {
        throw new IllegalStateException("Only run this on the client!");
    }

    @Override
    public SkinType getSkinType() {
        throw new IllegalStateException("Only run this on the client!");
    }

    @Override
    public void init() {
        KnappingPatterns.load();
        MoldingPatterns.load();
        Evolution.LOGGER.info("ServerProxy: Finished loading!");
    }

    @Override
    public void updateStats(Object2LongMap<Stat<?>> statsData) {
        throw new IllegalStateException("Only run this on the client!");
    }
}
