package tgw.evolution.util.hitbox;

import tgw.evolution.util.hitbox.hms.HM;

public class HitboxGroup implements HM, IRoot {

    private final IRoot parent;
    private float pivotX;
    private float pivotY;
    private float pivotZ;
    private float rotationX;
    private float rotationY;
    private float rotationZ;

    public HitboxGroup(IRoot parent) {
        this(parent, 0, 0, 0);
    }

    public HitboxGroup(IRoot parent, float x, float y, float z) {
        this.parent = parent;
        assert this != parent;
        this.setPivotX(x);
        this.setPivotY(y);
        this.setPivotZ(z);
    }

    @Override
    public void addRotationX(float x) {
        this.rotationX += x;
    }

    @Override
    public void addRotationY(float y) {
        this.rotationY += y;
    }

    @Override
    public void addRotationZ(float z) {
        this.rotationZ += z;
    }

    @Override
    public float getPivotX() {
        return this.pivotX * 16;
    }

    @Override
    public float getPivotY() {
        return this.pivotY * 16;
    }

    @Override
    public float getPivotZ() {
        return this.pivotZ * 16;
    }

    @Override
    public Matrix4d helperColliderTransform() {
        return this.parent.helperColliderTransform();
    }

    @Override
    public Matrix4d helperTransform() {
        return this.parent.helperTransform();
    }

    @Override
    public float scaleX() {
        return this.parent.scaleX();
    }

    @Override
    public float scaleY() {
        return this.parent.scaleY();
    }

    @Override
    public float scaleZ() {
        return this.parent.scaleZ();
    }

    @Override
    public void setPivotX(float x) {
        this.pivotX = x / 16.0f;
    }

    @Override
    public void setPivotY(float y) {
        this.pivotY = y / 16.0f;
    }

    @Override
    public void setPivotZ(float z) {
        this.pivotZ = z / 16.0f;
    }

    @Override
    public void setRotationX(float x) {
        this.rotationX = x;
    }

    @Override
    public void setRotationY(float y) {
        this.rotationY = y;
    }

    @Override
    public void setRotationZ(float z) {
        this.rotationZ = z;
    }

    @Override
    public void setVisible(boolean visible) {
        //Do nothing
    }

    @Override
    public void transformParent(Matrix4d matrix) {
        this.parent.transformParent(matrix);
        matrix.translate(this.pivotX, this.pivotY, this.pivotZ);
        matrix.rotateZRad(this.rotationZ);
        matrix.rotateYRad(this.rotationY);
        matrix.rotateXRad(this.rotationX);
    }

    @Override
    public double transformX(double x, double y, double z) {
        return this.parent.transformX(x, y, z);
    }

    @Override
    public double transformY(double x, double y, double z) {
        return this.parent.transformY(x, y, z);
    }

    @Override
    public double transformZ(double x, double y, double z) {
        return this.parent.transformZ(x, y, z);
    }

    @Override
    public void translateX(float x) {
        this.pivotX += x / 16.0f;
    }

    @Override
    public void translateY(float y) {
        this.pivotY += y / 16.0f;
    }

    @Override
    public void translateZ(float z) {
        this.pivotZ += z / 16.0f;
    }

    @Override
    public float xRot() {
        return this.rotationX;
    }

    @Override
    public float yRot() {
        return this.rotationY;
    }

    @Override
    public float zRot() {
        return this.rotationZ;
    }
}
