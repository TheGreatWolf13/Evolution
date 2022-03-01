package tgw.evolution.world.puzzle.pieces.config;

import tgw.evolution.util.math.MathHelper;

public class ConfigPuzzle<T extends ConfigPuzzle<T>> {

    private boolean isUnderground;
    private ForceType forceType = ForceType.NONE;
    private short desiredHeight = -1;
    private byte maxDeviation;

    public T underground() {
        this.isUnderground = true;
        return (T) this;
    }

    public T forceType(ForceType type) {
        this.forceType = type;
        return (T) this;
    }

    public T desiredHeight(int desiredHeight) {
        this.desiredHeight = (short) MathHelper.clamp(desiredHeight, 0, 255);
        return (T) this;
    }

    public T maxDeviation(int maxDeviation) {
        this.maxDeviation = (byte) MathHelper.clamp(maxDeviation, 0, 127);
        return (T) this;
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
