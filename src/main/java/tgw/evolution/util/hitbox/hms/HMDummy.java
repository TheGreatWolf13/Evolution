package tgw.evolution.util.hitbox.hms;

public final class HMDummy implements HM {

    public static final HM DUMMY = new HMDummy();

    private HMDummy() {
    }

    @Override
    public void addRotationX(float dx) {
    }

    @Override
    public void addRotationY(float dy) {
    }

    @Override
    public void addRotationZ(float dz) {
    }

    @Override
    public float getPivotX() {
        return 0;
    }

    @Override
    public float getPivotY() {
        return 0;
    }

    @Override
    public float getPivotZ() {
        return 0;
    }

    @Override
    public void setPivotX(float x) {
    }

    @Override
    public void setPivotY(float y) {
    }

    @Override
    public void setPivotZ(float z) {
    }

    @Override
    public void setRotationX(float rotX) {
    }

    @Override
    public void setRotationY(float rotY) {
    }

    @Override
    public void setRotationZ(float rotZ) {
    }

    @Override
    public void setVisible(boolean visible) {
        //Do nothing
    }

    @Override
    public void translateX(float x) {
    }

    @Override
    public void translateY(float y) {
    }

    @Override
    public void translateZ(float z) {
    }

    @Override
    public float xRot() {
        return 0;
    }

    @Override
    public float yRot() {
        return 0;
    }

    @Override
    public float zRot() {
        return 0;
    }
}
