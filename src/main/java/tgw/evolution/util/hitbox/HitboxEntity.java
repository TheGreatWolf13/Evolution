package tgw.evolution.util.hitbox;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public abstract class HitboxEntity<T extends Entity> {

    private final List<Hitbox> boxes;
    protected float ageInTicks;
    protected float pivotX;
    protected float pivotY;
    protected float pivotZ;
    protected float rotationPitch;
    protected float rotationX;
    protected float rotationY;
    protected float rotationYaw;
    protected float rotationZ;

    public HitboxEntity() {
        this.boxes = new ArrayList<>();
    }

    public static AxisAlignedBB aabb(double x0, double y0, double z0, double x1, double y1, double z1) {
        return new AxisAlignedBB(x0 / 16, y0 / 16, z0 / 16, x1 / 16, y1 / 16, z1 / 16);
    }

    public static AxisAlignedBB aabb(double x0, double y0, double z0, double x1, double y1, double z1, double scale) {
        return aabb(x0 * scale, y0 * scale, z0 * scale, x1 * scale, y1 * scale, z1 * scale);
    }

    public static Vec3d v(double x, double y, double z) {
        return new Vec3d(x / 16, y / 16, z / 16);
    }

    public static Vec3d v(double x, double y, double z, double scale) {
        return v(x * scale, y * scale, z * scale);
    }

    protected Hitbox addBox(BodyPart part, AxisAlignedBB aabb) {
        Hitbox box = new Hitbox(part, aabb, this);
        this.boxes.add(box);
        return box;
    }

    public void doOffset(double[] answer) {
        answer[0] += this.pivotX;
        answer[1] += this.pivotY;
        answer[2] += this.pivotZ;
    }

    public List<Hitbox> getBoxes() {
        return this.boxes;
    }

    public Vec3d getOffset() {
        return new Vec3d(this.pivotX, this.pivotY, this.pivotZ);
    }

    public Matrix3d getTransform() {
        Matrix3d xRot = new Matrix3d().asXRotation(this.rotationX);
        Matrix3d yRot = new Matrix3d().asYRotation(this.rotationY);
        Matrix3d zRot = new Matrix3d().asZRotation(this.rotationZ);
        return xRot.multiply(yRot).multiply(zRot);
    }

    public abstract void init(T entity, float partialTicks);

    protected void reset() {
        this.setPivot(0, 0, 0);
        this.setRotation(0, 0, 0);
        for (Hitbox box : this.boxes) {
            box.reset();
        }
    }

    protected void setPivot(float x, float y, float z, float scale) {
        this.setPivot(x * scale, y * scale, z * scale);
    }

    protected void setPivot(float x, float y, float z) {
        this.pivotX = x;
        this.pivotY = y;
        this.pivotZ = z;
    }

    protected void setRotation(float x, float y, float z) {
        this.rotationX = x;
        this.rotationY = y;
        this.rotationZ = z;
    }
}
