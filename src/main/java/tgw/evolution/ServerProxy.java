package tgw.evolution;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.stats.Stat;
import net.minecraft.world.World;
import net.minecraftforge.client.event.TextureStitchEvent;
import tgw.evolution.util.SkinType;

public class ServerProxy implements IProxy {

    @Override
    public void addTextures(TextureStitchEvent.Pre event) {
        throw new AssertionError("Should only run on the client!");
    }

    @Override
    public PlayerEntity getClientPlayer() {
        throw new AssertionError("Should only run on the client!");
    }

    @Override
    public World getClientWorld() {
        throw new AssertionError("Should only run on the client!");
    }

    @Override
    public SkinType getSkinType() {
        throw new AssertionError("Should only run on the client!");
    }

    @Override
    public void init() {
        IProxy.super.init();
        Evolution.LOGGER.info("ServerProxy: Finished loading!");
    }

    @Override
    public void updateStats(Object2LongMap<Stat<?>> statsData) {
        throw new AssertionError("Should only run on the client!");
    }
}
