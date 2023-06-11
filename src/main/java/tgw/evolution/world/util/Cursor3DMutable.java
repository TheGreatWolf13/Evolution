package tgw.evolution.world.util;

import org.intellij.lang.annotations.MagicConstant;

public class Cursor3DMutable {

    public static final int INSIDE = 0;
    public static final int FACE = 1;
    public static final int EDGE = 2;
    public static final int CORNER = 3;
    private int depth;
    private int end;
    /**
     * Bit 0: Should Recalculate X;<br>
     * Bit 1: Should Recalculate Z;<br>
     */
    private byte flagRecalc = 0b11;
    private int height;
    private int index;
    private int originX;
    private int originY;
    private int originZ;
    private int width;
    private int x;
    private int y;
    private int z;

    public boolean advance() {
        if (this.index == this.end) {
            return false;
        }
        if (this.index > 0) {
            if (++this.y == this.height) {
                this.y = 0;
                this.flagRecalc |= 1;
                if (++this.x == this.width) {
                    this.x = 0;
                    this.flagRecalc |= 2;
                    if (++this.z == this.depth) {
                        this.z = 0;
                    }
                }
            }
        }
        ++this.index;
        return true;
    }

    @MagicConstant(valuesFromClass = Cursor3DMutable.class)
    public int getNextType() {
        int i = INSIDE;
        if (this.x == 0 || this.x == this.width - 1) {
            ++i;
        }
        if (this.y == 0 || this.y == this.height - 1) {
            ++i;
        }
        if (this.z == 0 || this.z == this.depth - 1) {
            ++i;
        }
        return i;
    }

    public byte getRecalculationFlag() {
        byte old = this.flagRecalc;
        this.flagRecalc = 0;
        return old;
    }

    public int nextX() {
        return this.originX + this.x;
    }

    public int nextY() {
        return this.originY + this.y;
    }

    public int nextZ() {
        return this.originZ + this.z;
    }

    public void set(int originX, int originY, int originZ, int endX, int endY, int endZ) {
        this.originX = originX;
        this.originY = originY;
        this.originZ = originZ;
        this.width = endX - originX + 1;
        this.height = endY - originY + 1;
        this.depth = endZ - originZ + 1;
        this.end = this.width * this.height * this.depth;
        this.x = 0;
        this.y = 0;
        this.z = 0;
        this.index = 0;
        this.flagRecalc = 0b11;
    }
}
