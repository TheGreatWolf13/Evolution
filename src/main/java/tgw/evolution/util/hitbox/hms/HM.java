package tgw.evolution.util.hitbox.hms;

public interface HM {

    void addRotationX(float dx);

    void addRotationY(float dy);

    void addRotationZ(float dz);

    default void copy(HM hm) {
        this.setRotationX(hm.xRot());
        this.setRotationY(hm.yRot());
        this.setRotationZ(hm.zRot());
        this.setPivotX(hm.getPivotX());
        this.setPivotY(hm.getPivotY());
        this.setPivotZ(hm.getPivotZ());
    }

    float getPivotX();

    float getPivotY();

    float getPivotZ();

    default void invertRotationY() {
        this.setRotationY(-this.yRot());
    }

    void setPivotX(float x);

    void setPivotY(float y);

    void setPivotZ(float z);

    void setRotationX(float rotX);

    void setRotationY(float rotY);

    void setRotationZ(float rotZ);

    void setVisible(boolean visible);

    float xRot();

    float yRot();

    float zRot();
}
