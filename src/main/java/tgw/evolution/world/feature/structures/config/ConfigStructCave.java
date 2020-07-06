package tgw.evolution.world.feature.structures.config;

import tgw.evolution.world.puzzle.pieces.config.CivilizationType;

import java.util.Random;

public class ConfigStructCave implements IConfigStruct {

    private final Random random;
    private final boolean hasMega;
    private final float danger;
    private final CivilizationType type;
    private boolean megaGenerated;

    public ConfigStructCave(long seed, int x, int z) {
        this.random = new Random(seed * x + z);
        this.danger = this.random.nextFloat();
        this.type = CivilizationType.getRandom(this.random);
        this.hasMega = this.random.nextBoolean();
    }

    public boolean hasMega() {
        return this.hasMega;
    }

    public float getDanger() {
        return this.danger;
    }

    public CivilizationType getCivType() {
        return this.type;
    }

    public boolean hasMegaGenerated() {
        return this.megaGenerated;
    }

    public void setMegaGenerated() {
        this.megaGenerated = true;
    }

    public Random getRandom() {
        return this.random;
    }
}
