package tgw.evolution.util.math;

public class Vec2f {

    private float x;
    private float y;

    public Vec2f(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Vec2f() {
    }

    public float getX() {
        return this.x;
    }

    public float getY() {
        return this.y;
    }

    public void set(float x, float y) {
        this.x = x;
        this.y = y;
    }
}
