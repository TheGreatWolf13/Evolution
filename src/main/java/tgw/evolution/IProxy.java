package tgw.evolution;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import net.minecraft.stats.Stat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import tgw.evolution.blocks.tileentities.MoldingPatterns;
import tgw.evolution.util.constants.SkinType;
import tgw.evolution.util.hitbox.HitboxRegistry;
import tgw.evolution.util.toast.Toasts;

public interface IProxy {

    void addTextures(TextureStitchEvent.Pre event);

    Level getClientLevel();

    Player getClientPlayer();

    float getPartialTicks();

    SkinType getSkinType();

    default void init() {
        MoldingPatterns.load();
        Toasts.register();
        HitboxRegistry.register();
    }

    void registerModels(ModelRegistryEvent event);

    void updateStats(Object2LongMap<Stat<?>> statsData);
}
