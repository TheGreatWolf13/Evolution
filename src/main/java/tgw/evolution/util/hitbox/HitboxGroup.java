package tgw.evolution.util.hitbox;

import tgw.evolution.util.collection.RArrayList;
import tgw.evolution.util.collection.RList;

import java.util.Collections;

public class HitboxGroup implements IHitbox {

    private final RList<Hitbox> boxes = new RArrayList<>();
    private final RList<StartingRotation> rotations = new RArrayList<>();

    public HitboxGroup(Hitbox... boxes) {
        Collections.addAll(this.boxes, boxes);
        for (int i = 0; i < boxes.length; i++) {
            this.rotations.add(StartingRotation.ZERO);
        }
    }

    @Override
    public void addRotationX(float x) {
        for (Hitbox box : this.boxes) {
            box.rotationX += x;
        }
    }

    @Override
    public void addRotationY(float y) {
        for (Hitbox box : this.boxes) {
            box.rotationY += y;
        }
    }

    @Override
    public void addRotationZ(float z) {
        for (Hitbox box : this.boxes) {
            box.rotationZ += z;
        }
    }

    public void finish() {
        this.boxes.trimCollection();
        this.rotations.trimCollection();
    }

    @Override
    public float getRotationX() {
        if (this.boxes.isEmpty()) {
            throw new IllegalStateException("Empty group");
        }
        return this.boxes.get(0).rotationX;
    }

    @Override
    public float getRotationY() {
        if (this.boxes.isEmpty()) {
            throw new IllegalStateException("Empty group");
        }
        return this.boxes.get(0).rotationY;
    }

    @Override
    public float getRotationZ() {
        if (this.boxes.isEmpty()) {
            throw new IllegalStateException("Empty group");
        }
        return this.boxes.get(0).rotationZ;
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

    @Override
    public void setRotationX(float x) {
        for (int i = 0; i < this.boxes.size(); i++) {
            Hitbox box = this.boxes.get(i);
            box.rotationX = this.rotations.get(i).xRot + x;
        }
    }

    @Override
    public void setRotationY(float y) {
        for (int i = 0; i < this.boxes.size(); i++) {
            Hitbox box = this.boxes.get(i);
            box.rotationY = this.rotations.get(i).yRot + y;
        }
    }

    @Override
    public void setRotationZ(float z) {
        for (int i = 0; i < this.boxes.size(); i++) {
            Hitbox box = this.boxes.get(i);
            box.rotationZ = this.rotations.get(i).zRot + z;
        }
    }

    public void setStartingRotationForBox(int index, float xRot, float yRot, float zRot) {
        if (index < 0) {
            throw new ArrayIndexOutOfBoundsException("Negative index for Hitbox: " + index);
        }
        if (index >= this.rotations.size()) {
            throw new ArrayIndexOutOfBoundsException("HitboxGroup only contains " + this.rotations.size() + " hitboxes");
        }
        this.rotations.add(index, new StartingRotation(xRot, yRot, zRot));
        this.rotations.remove(index + 1);
    }

    public record StartingRotation(float xRot, float yRot, float zRot) {

        public static final StartingRotation ZERO = new StartingRotation(0, 0, 0);
    }
}
