package tgw.evolution;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import net.minecraft.stats.Stat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import tgw.evolution.util.constants.SkinType;

public class ServerProxy implements IProxy {

    @Override
    public void addTextures(TextureStitchEvent.Pre event) {
        throw new AssertionError("Should only run on the client!");
    }

    @Override
    public Level getClientLevel() {
        throw new AssertionError("Should only run on the client!");
    }

    @Override
    public Player getClientPlayer() {
        throw new AssertionError("Should only run on the client!");
    }

    @Override
    public float getPartialTicks() {
        return 1.0f;
    }

    @Override
    public SkinType getSkinType() {
        throw new AssertionError("Should only run on the client!");
    }

    @Override
    public void init() {
        IProxy.super.init();
        Evolution.info("Finished loading!");
    }

    @Override
    public void registerModels(ModelRegistryEvent event) {
        throw new AssertionError("Should only run on the client!");
    }

    @Override
    public void updateStats(Object2LongMap<Stat<?>> statsData) {
        throw new AssertionError("Should only run on the client!");
    }
}
