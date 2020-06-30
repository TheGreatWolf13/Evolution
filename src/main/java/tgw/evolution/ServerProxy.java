package tgw.evolution;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import tgw.evolution.blocks.tileentities.KnappingPatterns;
import tgw.evolution.blocks.tileentities.MoldingPatterns;

public class ServerProxy implements IProxy {

    @Override
    public World getClientWorld() {
        throw new IllegalStateException("Only run this on the client!");
    }

    @Override
    public PlayerEntity getClientPlayer() {
        throw new IllegalStateException("Only run this on the client!");
    }

    @Override
    public void init() {
        KnappingPatterns.load();
        MoldingPatterns.load();
    }
}
