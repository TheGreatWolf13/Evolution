package tgw.evolution;

import net.minecraft.entity.player.PlayerEntity;
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
    }
}
