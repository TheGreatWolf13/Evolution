package tgw.evolution.world.puzzle.pieces.config;

import tgw.evolution.util.MathHelper;

public class CaveConfigPuzzle extends ConfigPuzzle {

    private float danger;
    private float civilization;
    private CivilizationType type = CivilizationType.NORMAL;

    public CaveConfigPuzzle danger(float danger) {
        this.danger = MathHelper.clamp(danger, 0, 1);
        return this;
    }

    public CaveConfigPuzzle civ(float civilization) {
        this.civilization = MathHelper.clamp(civilization, 0, 1);
        return this;
    }

    public CaveConfigPuzzle civType(CivilizationType type) {
        this.type = type;
        return this;
    }

    public float getDanger() {
        return this.danger;
    }

    public float getCivilization() {
        return this.civilization;
    }

    public CivilizationType getCivType() {
        return this.type;
    }
}
