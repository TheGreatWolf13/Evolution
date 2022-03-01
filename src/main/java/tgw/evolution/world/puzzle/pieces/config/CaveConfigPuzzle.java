package tgw.evolution.world.puzzle.pieces.config;

import tgw.evolution.util.math.MathHelper;

public class CaveConfigPuzzle extends ConfigPuzzle<CaveConfigPuzzle> {

    private float danger = -1;
    private boolean isMega;
    private CivilizationType type = CivilizationType.NORMAL;

    public CaveConfigPuzzle danger(float danger) {
        this.danger = MathHelper.clamp(danger, 0, 1);
        return this;
    }

    public CaveConfigPuzzle civ(CivilizationType type) {
        this.type = type;
        return this;
    }

    public CaveConfigPuzzle mega() {
        this.isMega = true;
        return this;
    }

    public boolean isMega() {
        return this.isMega;
    }

    public float getDanger() {
        return this.danger;
    }

    public CivilizationType getCiv() {
        return this.type;
    }
}
