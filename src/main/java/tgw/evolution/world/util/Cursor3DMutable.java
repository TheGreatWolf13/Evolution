package tgw.evolution.world.util;

public class Cursor3DMutable {

    private int depth;
    private int end;
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
            if (++this.x == this.width) {
                this.x = 0;
                if (++this.y == this.height) {
                    this.y = 0;
                    if (++this.z == this.depth) {
                        this.z = 0;
                    }
                }
            }
        }
        ++this.index;
        return true;
    }

    public int getNextType() {
        int i = 0;
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
    }
}
