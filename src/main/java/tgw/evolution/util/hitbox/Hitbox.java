package tgw.evolution.util.hitbox;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.util.hitbox.hms.HM;
import tgw.evolution.util.math.MathHelper;
import tgw.evolution.util.math.Vec3d;

public class Hitbox implements HM {

    private final AABB aabb;
    private final HitboxType part;
    protected @Nullable HitboxEntity<?> parent;
    private float pivotX;
    private float pivotY;
    private float pivotZ;
    private float rotationX;
    private float rotationY;
    private float rotationZ;

    public Hitbox(HitboxType part, AABB aabb, @Nullable HitboxEntity<?> parent) {
        this.part = part;
        this.aabb = aabb;
        this.parent = parent;
    }

    private static void drawEdge(VertexConsumer buffer,
                                 Matrix4f pose,
                                 Matrix3f normal,
                                 float x,
                                 float y,
                                 float z,
                                 float x0,
                                 float y0,
                                 float z0,
                                 float x1,
                                 float y1,
                                 float z1,
                                 float r,
                                 float g,
                                 float b,
                                 float a) {
        //Calculate normals
        float nx = x1 - x0;
        float ny = y1 - y0;
        float nz = z1 - z0;
        float norm = Mth.fastInvSqrt(nx * nx + ny * ny + nz * nz);
        nx *= norm;
        ny *= norm;
        nz *= norm;
        //Fill vertices
        buffer.vertex(pose, x0 + x, y0 + y, z0 + z)
              .color(r, g, b, a)
              .normal(normal, nx, ny, nz)
              .endVertex();
        buffer.vertex(pose, x1 + x, y1 + y, z1 + z)
              .color(r, g, b, a)
              .normal(normal, nx, ny, nz)
              .endVertex();
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

    public Matrix4d adjustedColliderTransform() {
        assert this.parent != null;
        Matrix4d matrix = this.parent.helperColliderTransform;
        matrix.setIdentity();
        matrix.translate(this.pivotX, this.pivotY, this.pivotZ);
        matrix.rotateZRad(this.rotationZ);
        matrix.rotateYRad(this.rotationY);
        matrix.rotateXRad(this.rotationX);
        matrix.scale(this.parent.scaleX, this.parent.scaleY, this.parent.scaleZ);
        return matrix;
    }

    public Matrix4d adjustedTransform() {
        assert this.parent != null;
        Matrix4d matrix = this.parent.helperTransform;
        matrix.setIdentity();
        matrix.translate(this.pivotX, this.pivotY, this.pivotZ);
        matrix.rotateZRad(this.rotationZ);
        matrix.rotateYRad(this.rotationY);
        matrix.rotateXRad(this.rotationX);
        matrix.scale(this.parent.scaleX, this.parent.scaleY, this.parent.scaleZ);
        return matrix;
    }

    public double clipDist(Vec3 from, Vec3 to) {
        return this.clipDist(from.x, from.y, from.z, to.x, to.y, to.z);
    }

    public double clipDist(double x0, double y0, double z0, double x1, double y1, double z1) {
        double dx = x1 - x0;
        double dy = y1 - y0;
        double dz = z1 - z0;
        return this.getMinDist(x0, y0, z0, dx, dy, dz);
    }

    public boolean contains(double x, double y, double z) {
        return this.aabb.contains(x, y, z);
    }

    public boolean contains(Vec3d vec) {
        return this.aabb.contains(vec);
    }

    protected void drawEdges(Matrix4d transform,
                             VertexConsumer buffer,
                             Matrix4f pose,
                             Matrix3f normal,
                             float x,
                             float y,
                             float z,
                             float r,
                             float g,
                             float b,
                             float a) {
        double minX = this.minX();
        double maxX = this.maxX();
        double minY = this.minY();
        double maxY = this.maxY();
        double minZ = this.minZ();
        double maxZ = this.maxZ();
        //000
        double x000 = transform.transformX(minX, minY, minZ);
        double y000 = transform.transformY(minX, minY, minZ);
        double z000 = transform.transformZ(minX, minY, minZ);
        assert this.parent != null;
        float x000a = (float) this.parent.transformX(x000, y000, z000);
        float y000a = (float) this.parent.transformY(x000, y000, z000);
        float z000a = (float) this.parent.transformZ(x000, y000, z000);
        //001
        double x001 = transform.transformX(minX, minY, maxZ);
        double y001 = transform.transformY(minX, minY, maxZ);
        double z001 = transform.transformZ(minX, minY, maxZ);
        float x001a = (float) this.parent.transformX(x001, y001, z001);
        float y001a = (float) this.parent.transformY(x001, y001, z001);
        float z001a = (float) this.parent.transformZ(x001, y001, z001);
        //010
        double x010 = transform.transformX(minX, maxY, minZ);
        double y010 = transform.transformY(minX, maxY, minZ);
        double z010 = transform.transformZ(minX, maxY, minZ);
        float x010a = (float) this.parent.transformX(x010, y010, z010);
        float y010a = (float) this.parent.transformY(x010, y010, z010);
        float z010a = (float) this.parent.transformZ(x010, y010, z010);
        //011
        double x011 = transform.transformX(minX, maxY, maxZ);
        double y011 = transform.transformY(minX, maxY, maxZ);
        double z011 = transform.transformZ(minX, maxY, maxZ);
        float x011a = (float) this.parent.transformX(x011, y011, z011);
        float y011a = (float) this.parent.transformY(x011, y011, z011);
        float z011a = (float) this.parent.transformZ(x011, y011, z011);
        //100
        double x100 = transform.transformX(maxX, minY, minZ);
        double y100 = transform.transformY(maxX, minY, minZ);
        double z100 = transform.transformZ(maxX, minY, minZ);
        float x100a = (float) this.parent.transformX(x100, y100, z100);
        float y100a = (float) this.parent.transformY(x100, y100, z100);
        float z100a = (float) this.parent.transformZ(x100, y100, z100);
        //101
        double x101 = transform.transformX(maxX, minY, maxZ);
        double y101 = transform.transformY(maxX, minY, maxZ);
        double z101 = transform.transformZ(maxX, minY, maxZ);
        float x101a = (float) this.parent.transformX(x101, y101, z101);
        float y101a = (float) this.parent.transformY(x101, y101, z101);
        float z101a = (float) this.parent.transformZ(x101, y101, z101);
        //110
        double x110 = transform.transformX(maxX, maxY, minZ);
        double y110 = transform.transformY(maxX, maxY, minZ);
        double z110 = transform.transformZ(maxX, maxY, minZ);
        float x110a = (float) this.parent.transformX(x110, y110, z110);
        float y110a = (float) this.parent.transformY(x110, y110, z110);
        float z110a = (float) this.parent.transformZ(x110, y110, z110);
        //111
        double x111 = transform.transformX(maxX, maxY, maxZ);
        double y111 = transform.transformY(maxX, maxY, maxZ);
        double z111 = transform.transformZ(maxX, maxY, maxZ);
        float x111a = (float) this.parent.transformX(x111, y111, z111);
        float y111a = (float) this.parent.transformY(x111, y111, z111);
        float z111a = (float) this.parent.transformZ(x111, y111, z111);
        //X Axis
        drawEdge(buffer, pose, normal, x, y, z, x000a, y000a, z000a, x100a, y100a, z100a, r, g, b, a);
        drawEdge(buffer, pose, normal, x, y, z, x001a, y001a, z001a, x101a, y101a, z101a, r, g, b, a);
        drawEdge(buffer, pose, normal, x, y, z, x010a, y010a, z010a, x110a, y110a, z110a, r, g, b, a);
        drawEdge(buffer, pose, normal, x, y, z, x011a, y011a, z011a, x111a, y111a, z111a, r, g, b, a);
        //Y Axis
        drawEdge(buffer, pose, normal, x, y, z, x000a, y000a, z000a, x010a, y010a, z010a, r, g, b, a);
        drawEdge(buffer, pose, normal, x, y, z, x001a, y001a, z001a, x011a, y011a, z011a, r, g, b, a);
        drawEdge(buffer, pose, normal, x, y, z, x100a, y100a, z100a, x110a, y110a, z110a, r, g, b, a);
        drawEdge(buffer, pose, normal, x, y, z, x101a, y101a, z101a, x111a, y111a, z111a, r, g, b, a);
        //Z Axis
        drawEdge(buffer, pose, normal, x, y, z, x000a, y000a, z000a, x001a, y001a, z001a, r, g, b, a);
        drawEdge(buffer, pose, normal, x, y, z, x010a, y010a, z010a, x011a, y011a, z011a, r, g, b, a);
        drawEdge(buffer, pose, normal, x, y, z, x100a, y100a, z100a, x101a, y101a, z101a, r, g, b, a);
        drawEdge(buffer, pose, normal, x, y, z, x110a, y110a, z110a, x111a, y111a, z111a, r, g, b, a);
    }

    private double getMinDist(double x0, double y0, double z0, double dx, double dy, double dz) {
        double minDist = Double.NaN;
        if (dx > 1.0E-7) {
            minDist = MathHelper.clipPoint(minDist, dx, dy, dz, this.minX(), this.minY(), this.maxY(), this.minZ(), this.maxZ(), x0, y0, z0);
        }
        else if (dx < -1.0E-7) {
            minDist = MathHelper.clipPoint(minDist, dx, dy, dz, this.maxX(), this.minY(), this.maxY(), this.minZ(), this.maxZ(), x0, y0, z0);
        }
        if (dy > 1.0E-7) {
            minDist = MathHelper.clipPoint(minDist, dy, dz, dx, this.minY(), this.minZ(), this.maxZ(), this.minX(), this.maxX(), y0, z0, x0);
        }
        else if (dy < -1.0E-7) {
            minDist = MathHelper.clipPoint(minDist, dy, dz, dx, this.maxY(), this.minZ(), this.maxZ(), this.minX(), this.maxX(), y0, z0, x0);
        }
        if (dz > 1.0E-7) {
            return MathHelper.clipPoint(minDist, dz, dx, dy, this.minZ(), this.minX(), this.maxX(), this.minY(), this.maxY(), z0, x0, y0);
        }
        if (dz < -1.0E-7) {
            return MathHelper.clipPoint(minDist, dz, dx, dy, this.maxZ(), this.minX(), this.maxX(), this.minY(), this.maxY(), z0, x0, y0);
        }
        return minDist;
    }

    public @Nullable HitboxEntity<?> getParent() {
        return this.parent;
    }

    public HitboxType getPart() {
        return this.part;
    }

    @Override
    public float getPivotX() {
        return this.pivotX * 16.0f;
    }

    @Override
    public float getPivotY() {
        return this.pivotY * 16.0f;
    }

    @Override
    public float getPivotZ() {
        return this.pivotZ * 16.0f;
    }

    public double maxX() {
        return this.aabb.maxX;
    }

    public double maxY() {
        return this.aabb.maxY;
    }

    public double maxZ() {
        return this.aabb.maxZ;
    }

    public double minX() {
        return this.aabb.minX;
    }

    public double minY() {
        return this.aabb.minY;
    }

    public double minZ() {
        return this.aabb.minZ;
    }

    protected void reset() {
        this.pivotX = 0;
        this.pivotY = 0;
        this.pivotZ = 0;
        this.rotationX = 0;
        this.rotationY = 0;
        this.rotationZ = 0;
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
    public String toString() {
        return "Hitbox{" + "part=" + this.part + ", aabb=" + this.aabb + '}';
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
