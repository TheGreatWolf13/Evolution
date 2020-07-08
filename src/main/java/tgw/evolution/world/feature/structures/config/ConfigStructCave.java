package tgw.evolution.world.feature.structures.config;

import tgw.evolution.world.puzzle.pieces.config.CivilizationType;

import java.util.Random;

public class ConfigStructCave implements IConfigStruct {

    private final Random random;
    private final boolean hasEntrance;
    private final boolean hasMega;
    private final float danger;
    private final CivilizationType type;
    private boolean megaGenerated;

    public ConfigStructCave(long seed, int x, int z) {
        this.random = new Random(seed * x + z); //set a seed to be used
        //
        this.hasEntrance = this.random.nextBoolean(); //set if has entrance
        this.danger = this.random.nextFloat(); //set the danger of the cave
        this.type = CivilizationType.getRandom(this.random); //set the type of civilization
        this.hasMega = this.random.nextBoolean(); //set if it has a mega, if true then it CAN have a mega, if false then it will never have a mega
    }

    public boolean hasMega() {
        return this.hasMega;
    }

    public boolean hasEntrance() {
        return this.hasEntrance;
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
