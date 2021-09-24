package tgw.evolution;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.stats.Stat;
import net.minecraft.world.World;
import net.minecraftforge.client.event.TextureStitchEvent;
import tgw.evolution.blocks.tileentities.MoldingPatterns;
import tgw.evolution.util.SkinType;

public interface IProxy {

    void addTextures(TextureStitchEvent.Pre event);

    PlayerEntity getClientPlayer();

    World getClientWorld();

    SkinType getSkinType();

    default void init() {
        MoldingPatterns.load();
    }

    void updateStats(Object2LongMap<Stat<?>> statsData);
}
