package tgw.evolution.world.puzzle.pieces.config;

import tgw.evolution.util.MathHelper;

public class ConfigPuzzle {

    private boolean isUnderground;
    private ForceType forceType = ForceType.NONE;
    private short desiredHeight = -1;
    private byte maxDeviation;

    public ConfigPuzzle underground() {
        this.isUnderground = true;
        return this;
    }

    public ConfigPuzzle forceType(ForceType type) {
        this.forceType = type;
        return this;
    }

    public ConfigPuzzle desiredHeight(int desiredHeight) {
        this.desiredHeight = (short) MathHelper.clamp(desiredHeight, 0, 255);
        return this;
    }

    public ConfigPuzzle maxDeviation(int maxDeviation) {
        this.maxDeviation = (byte) MathHelper.clamp(maxDeviation, 0, 127);
        return this;
    }

    public boolean isUnderground() {
        return this.isUnderground;
    }

    public ForceType getForceType() {
        return this.forceType;
    }

    public short getDesiredHeight() {
        return this.desiredHeight;
    }

    public byte getMaxDeviation() {
        return this.maxDeviation;
    }
}
