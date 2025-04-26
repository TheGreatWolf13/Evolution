package tgw.evolution.patches;

import net.minecraft.client.color.item.ItemColors;
import tgw.evolution.client.renderer.chunk.LevelRenderer;
import tgw.evolution.patches.replace.net.minecraft.client.particle.ParticleEngine;

public interface PatchMinecraft {

    default ItemColors getItemColors() {
        throw new AbstractMethodError();
    }

    default boolean isMultiplayerPaused() {
        throw new AbstractMethodError();
    }

    default LevelRenderer levelRenderer() {
        throw new AbstractMethodError();
    }

    default ParticleEngine particleEngine() {
        throw new AbstractMethodError();
    }

    default void resetUseHeld() {
        throw new AbstractMethodError();
    }

    default void setMultiplayerPaused(boolean paused) {
        throw new AbstractMethodError();
    }
}
