package tgw.evolution.util.hitbox;

import net.minecraft.util.math.vector.Vector3d;
import tgw.evolution.util.MathHelper;

public class Matrix3d {

    private double m00;
    private double m01;
    private double m02;
    private double m10;
    private double m11;
    private double m12;
    private double m20;
    private double m21;
    private double m22;

    public Matrix3d add(Matrix3d matrix) {
        this.m00 += matrix.m00;
        this.m01 += matrix.m01;
        this.m02 += matrix.m02;
        this.m10 += matrix.m10;
        this.m11 += matrix.m11;
        this.m12 += matrix.m12;
        this.m20 += matrix.m20;
        this.m21 += matrix.m21;
        this.m22 += matrix.m22;
        return this;
    }

    public Matrix3d asIdentity() {
        this.m00 = this.m11 = this.m22 = 1;
        this.m01 = this.m02 = this.m10 = this.m12 = this.m20 = this.m21 = 0;
        return this;
    }

    public Matrix3d asXRotation(float radians) {
        this.asIdentity();
        if (radians == 0) {
            return this;
        }
        double s = MathHelper.sin(radians);
        double c = MathHelper.cos(radians);
        this.m22 = this.m11 = c;
        this.m21 = s;
        this.m12 = -s;
        return this;
    }

    public Matrix3d asYRotation(float radians) {
        this.asIdentity();
        if (radians == 0) {
            return this;
        }
        double s = MathHelper.sin(radians);
        double c = MathHelper.cos(radians);
        this.m00 = this.m22 = c;
        this.m20 = s;
        this.m02 = -s;
        return this;
    }

    public Matrix3d asZRotation(float radians) {
        this.asIdentity();
        if (radians == 0) {
            return this;
        }
        double s = MathHelper.sin(radians);
        double c = MathHelper.cos(radians);
        this.m00 = this.m11 = c;
        this.m01 = -s;
        this.m10 = s;
        return this;
    }

    public Matrix3d copy() {
        return new Matrix3d().add(this);
    }

    public Matrix3d multiply(Matrix3d m) {
        double new00 = this.m00 * m.m00 + this.m01 * m.m10 + this.m02 * m.m20;
        double new01 = this.m00 * m.m01 + this.m01 * m.m11 + this.m02 * m.m21;
        double new02 = this.m00 * m.m02 + this.m01 * m.m12 + this.m02 * m.m22;
        double new10 = this.m10 * m.m00 + this.m11 * m.m10 + this.m12 * m.m20;
        double new11 = this.m10 * m.m01 + this.m11 * m.m11 + this.m12 * m.m21;
        double new12 = this.m10 * m.m02 + this.m11 * m.m12 + this.m12 * m.m22;
        double new20 = this.m20 * m.m00 + this.m21 * m.m10 + this.m22 * m.m20;
        double new21 = this.m20 * m.m01 + this.m21 * m.m11 + this.m22 * m.m21;
        double new22 = this.m20 * m.m02 + this.m21 * m.m12 + this.m22 * m.m22;
        this.m00 = new00;
        this.m01 = new01;
        this.m02 = new02;
        this.m10 = new10;
        this.m11 = new11;
        this.m12 = new12;
        this.m20 = new20;
        this.m21 = new21;
        this.m22 = new22;
        return this;
    }

    public Matrix3d scale(double d) {
        this.m00 *= d;
        this.m11 *= d;
        this.m22 *= d;
        return this;
    }

    @Override
    public String toString() {
        return "Matrix3d{" +
               this.m00 +
               ", " +
               this.m01 +
               ", " +
               this.m02 +
               "| " +
               this.m10 +
               ", " +
               this.m11 +
               ", " +
               this.m12 +
               "| " +
               this.m20 +
               ", " +
               this.m21 +
               ", " +
               this.m22 +
               '}';
    }

    public Vector3d transform(Vector3d vec) {
        double x = vec.x * this.m00 + vec.y * this.m01 + vec.z * this.m02;
        double y = vec.x * this.m10 + vec.y * this.m11 + vec.z * this.m12;
        double z = vec.x * this.m20 + vec.y * this.m21 + vec.z * this.m22;
        return new Vector3d(x, y, z);
    }

    public void transform(double x, double y, double z, double[] answer) {
        answer[0] = x * this.m00 + y * this.m01 + z * this.m02;
        answer[1] = x * this.m10 + y * this.m11 + z * this.m12;
        answer[2] = x * this.m20 + y * this.m21 + z * this.m22;
    }

    public Matrix3d transpose() {
        double d = this.m01;
        this.m01 = this.m10;
        this.m10 = d;
        d = this.m02;
        this.m02 = this.m20;
        this.m20 = d;
        d = this.m12;
        this.m12 = this.m21;
        this.m21 = d;
        return this;
    }
}
