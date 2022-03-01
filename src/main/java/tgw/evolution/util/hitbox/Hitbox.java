package tgw.evolution.util.hitbox;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import tgw.evolution.util.math.ILineConsumer;

public class Hitbox implements IHitbox {

    private final AABB aabb;
    private final HitboxEntity<? extends Entity> parent;
    private final HitboxType part;
    protected float pivotX;
    protected float pivotY;
    protected float pivotZ;
    protected float rotationX;
    protected float rotationY;
    protected float rotationZ;

    public Hitbox(HitboxType part, AABB aabb, HitboxEntity<? extends Entity> parent) {
        this.part = part;
        this.aabb = aabb;
        this.parent = parent;
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

    public void doOffset(double[] answer) {
        answer[0] += this.pivotX;
        answer[1] += this.pivotY;
        answer[2] += this.pivotZ;
    }

    public void forEachEdge(ILineConsumer consumer) {
        //X Axis
        consumer.consume(this.aabb.minX, this.aabb.minY, this.aabb.minZ, this.aabb.maxX, this.aabb.minY, this.aabb.minZ);
        consumer.consume(this.aabb.minX, this.aabb.maxY, this.aabb.minZ, this.aabb.maxX, this.aabb.maxY, this.aabb.minZ);
        consumer.consume(this.aabb.minX, this.aabb.minY, this.aabb.maxZ, this.aabb.maxX, this.aabb.minY, this.aabb.maxZ);
        consumer.consume(this.aabb.minX, this.aabb.maxY, this.aabb.maxZ, this.aabb.maxX, this.aabb.maxY, this.aabb.maxZ);
        //Y Axis
        consumer.consume(this.aabb.minX, this.aabb.minY, this.aabb.minZ, this.aabb.minX, this.aabb.maxY, this.aabb.minZ);
        consumer.consume(this.aabb.maxX, this.aabb.minY, this.aabb.minZ, this.aabb.maxX, this.aabb.maxY, this.aabb.minZ);
        consumer.consume(this.aabb.minX, this.aabb.minY, this.aabb.maxZ, this.aabb.minX, this.aabb.maxY, this.aabb.maxZ);
        consumer.consume(this.aabb.maxX, this.aabb.minY, this.aabb.maxZ, this.aabb.maxX, this.aabb.maxY, this.aabb.maxZ);
        //Z Axis
        consumer.consume(this.aabb.minX, this.aabb.minY, this.aabb.minZ, this.aabb.minX, this.aabb.minY, this.aabb.maxZ);
        consumer.consume(this.aabb.maxX, this.aabb.minY, this.aabb.minZ, this.aabb.maxX, this.aabb.minY, this.aabb.maxZ);
        consumer.consume(this.aabb.minX, this.aabb.maxY, this.aabb.minZ, this.aabb.minX, this.aabb.maxY, this.aabb.maxZ);
        consumer.consume(this.aabb.maxX, this.aabb.maxY, this.aabb.minZ, this.aabb.maxX, this.aabb.maxY, this.aabb.maxZ);
    }

    public AABB getAABB() {
        return this.aabb;
    }

    public Vec3 getOffset() {
        return new Vec3(this.pivotX, this.pivotY, this.pivotZ);
    }

    public <T extends Entity> HitboxEntity<T> getParent() {
        return (HitboxEntity<T>) this.parent;
    }

    public HitboxType getPart() {
        return this.part;
    }

    @Override
    public float getRotationX() {
        return this.rotationX;
    }

    @Override
    public float getRotationY() {
        return this.rotationY;
    }

    @Override
    public float getRotationZ() {
        return this.rotationZ;
    }

    public Matrix3d getTransformation() {
        Matrix3d xRot = new Matrix3d().asXRotation(this.rotationX);
        Matrix3d yRot = new Matrix3d().asYRotation(this.rotationY);
        Matrix3d zRot = new Matrix3d().asZRotation(this.rotationZ);
        return xRot.multiply(yRot).multiply(zRot);
    }

    protected void reset() {
        this.setPivot(0, 0, 0);
        this.setRotation(0, 0, 0);
    }

    protected void setPivot(float x, float y, float z) {
        this.pivotX = x;
        this.pivotY = y;
        this.pivotZ = z;
    }

    protected void setPivot(float x, float y, float z, float scale) {
        this.pivotX = x * scale;
        this.pivotY = y * scale;
        this.pivotZ = z * scale;
    }

    protected void setRotation(float x, float y, float z) {
        this.rotationX = x;
        this.rotationY = y;
        this.rotationZ = z;
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
    public String toString() {
        return "Hitbox{" + "part=" + this.part + ", aabb=" + this.aabb + '}';
    }
}
