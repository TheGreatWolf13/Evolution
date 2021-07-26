package tgw.evolution.util.hitbox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HitboxGroup {

    private final List<Hitbox> boxes = new ArrayList<>();

    public HitboxGroup(Hitbox... boxes) {
        Collections.addAll(this.boxes, boxes);
    }

    public void addRotationX(float x) {
        for (Hitbox box : this.boxes) {
            box.rotationX += x;
        }
    }

    public void addRotationY(float y) {
        for (Hitbox box : this.boxes) {
            box.rotationY += y;
        }
    }

    public void addRotationZ(float z) {
        for (Hitbox box : this.boxes) {
            box.rotationZ += z;
        }
    }

    public float getRotationX() {
        if (this.boxes.isEmpty()) {
            throw new IllegalStateException("Empty group");
        }
        return this.boxes.get(0).rotationX;
    }

    public void setRotationX(float x) {
        for (Hitbox box : this.boxes) {
            box.rotationX = x;
        }
    }

    public float getRotationY() {
        if (this.boxes.isEmpty()) {
            throw new IllegalStateException("Empty group");
        }
        return this.boxes.get(0).rotationY;
    }

    public void setRotationY(float y) {
        for (Hitbox box : this.boxes) {
            box.rotationY = y;
        }
    }

    public float getRotationZ() {
        if (this.boxes.isEmpty()) {
            throw new IllegalStateException("Empty group");
        }
        return this.boxes.get(0).rotationZ;
    }

    public void setRotationZ(float z) {
        for (Hitbox box : this.boxes) {
            box.rotationZ = z;
        }
    }

    public void setPivot(float x, float y, float z) {
        for (Hitbox box : this.boxes) {
            box.setPivot(x, y, z);
        }
    }

    public void setPivot(float x, float y, float z, float scale) {
        this.setPivot(x * scale, y * scale, z * scale);
    }

    public void setPivotX(float x) {
        for (Hitbox box : this.boxes) {
            box.pivotX = x;
        }
    }

    public void setPivotY(float y) {
        for (Hitbox box : this.boxes) {
            box.pivotY = y;
        }
    }

    public void setPivotZ(float z) {
        for (Hitbox box : this.boxes) {
            box.pivotZ = z;
        }
    }
}
