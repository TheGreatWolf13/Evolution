package tgw.evolution.util.hitbox;

import tgw.evolution.util.collection.RArrayList;
import tgw.evolution.util.collection.RList;
import tgw.evolution.util.hitbox.hms.HM;

import java.util.Collections;

public class HitboxGroup implements HM {

    private final RList<Hitbox> boxes = new RArrayList<>();
//    private final RList<StartingRotation> rotations = new RArrayList<>();

    public HitboxGroup(Hitbox... boxes) {
        Collections.addAll(this.boxes, boxes);
//        for (int i = 0; i < boxes.length; i++) {
//            this.rotations.add(StartingRotation.ZERO);
//        }
    }

    @Override
    public void addRotationX(float x) {
        for (int i = 0, l = this.boxes.size(); i < l; i++) {
            this.boxes.get(i).addRotationX(x);
        }
    }

    @Override
    public void addRotationY(float y) {
        for (int i = 0, l = this.boxes.size(); i < l; i++) {
            this.boxes.get(i).addRotationY(y);
        }
    }

    @Override
    public void addRotationZ(float z) {
        for (int i = 0, l = this.boxes.size(); i < l; i++) {
            this.boxes.get(i).addRotationZ(z);
        }
    }

    public void finish() {
        this.boxes.trimCollection();
//        this.rotations.trimCollection();
    }

    @Override
    public float getPivotX() {
        if (this.boxes.isEmpty()) {
            throw new IllegalStateException("Empty group");
        }
        return this.boxes.get(0).getPivotX();
    }

    @Override
    public float getPivotY() {
        if (this.boxes.isEmpty()) {
            throw new IllegalStateException("Empty group");
        }
        return this.boxes.get(0).getPivotY();
    }

    @Override
    public float getPivotZ() {
        if (this.boxes.isEmpty()) {
            throw new IllegalStateException("Empty group");
        }
        return this.boxes.get(0).getPivotZ();
    }

    @Override
    public void setPivotX(float x) {
        for (int i = 0, l = this.boxes.size(); i < l; i++) {
            this.boxes.get(i).setPivotX(x);
        }
    }

    @Override
    public void setPivotY(float y) {
        for (int i = 0, l = this.boxes.size(); i < l; i++) {
            this.boxes.get(i).setPivotY(y);
        }
    }

    @Override
    public void setPivotZ(float z) {
        for (int i = 0, l = this.boxes.size(); i < l; i++) {
            this.boxes.get(i).setPivotZ(z);
        }
    }

    @Override
    public void setRotationX(float x) {
        for (int i = 0; i < this.boxes.size(); i++) {
            Hitbox box = this.boxes.get(i);
            box.setRotationX(x/* + this.rotations.get(i).xRot*/);
        }
    }

    @Override
    public void setRotationY(float y) {
        for (int i = 0; i < this.boxes.size(); i++) {
            Hitbox box = this.boxes.get(i);
            box.setRotationY(y/* + this.rotations.get(i).yRot*/);
        }
    }

    @Override
    public void setRotationZ(float z) {
        for (int i = 0; i < this.boxes.size(); i++) {
            Hitbox box = this.boxes.get(i);
            box.setRotationZ(z/* + this.rotations.get(i).zRot*/);
        }
    }

//    public void setStartingRotationForBox(int index, float xRot, float yRot, float zRot) {
//        if (index < 0) {
//            throw new ArrayIndexOutOfBoundsException("Negative index for Hitbox: " + index);
//        }
//        if (index >= this.rotations.size()) {
//            throw new ArrayIndexOutOfBoundsException("HitboxGroup only contains " + this.rotations.size() + " hitboxes");
//        }
//        this.rotations.add(index, new StartingRotation(xRot, yRot, zRot));
//        this.rotations.remove(index + 1);
//    }

    @Override
    public void setVisible(boolean visible) {
        //Do nothing
    }

    @Override
    public float xRot() {
        if (this.boxes.isEmpty()) {
            throw new IllegalStateException("Empty group");
        }
        return this.boxes.get(0).xRot()/* - this.rotations.get(0).xRot*/;
    }

    @Override
    public float yRot() {
        if (this.boxes.isEmpty()) {
            throw new IllegalStateException("Empty group");
        }
        return this.boxes.get(0).yRot()/* - this.rotations.get(0).yRot*/;
    }

    @Override
    public float zRot() {
        if (this.boxes.isEmpty()) {
            throw new IllegalStateException("Empty group");
        }
        return this.boxes.get(0).zRot()/* - this.rotations.get(0).zRot*/;
    }

//    public record StartingRotation(float xRot, float yRot, float zRot) {
//
//        public static final StartingRotation ZERO = new StartingRotation(0, 0, 0);
//    }
}
