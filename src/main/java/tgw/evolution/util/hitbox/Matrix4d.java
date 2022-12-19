package tgw.evolution.util.hitbox;

import net.minecraft.util.Mth;
import tgw.evolution.util.math.Vec3d;

public class Matrix4d {

    private double m00;
    private double m01;
    private double m02;
    private double m03;
    private double m10;
    private double m11;
    private double m12;
    private double m13;
    private double m20;
    private double m21;
    private double m22;
    private double m23;
    private double scaleX;
    private double scaleY;
    private double scaleZ;

    public double postUntransformX(double x, double y, double z) {
        return x * this.m00 + y * this.m10 + z * this.m20;
    }

    public double postUntransformY(double x, double y, double z) {
        return x * this.m01 + y * this.m11 + z * this.m21;
    }

    public double postUntransformZ(double x, double y, double z) {
        return x * this.m02 + y * this.m12 + z * this.m22;
    }

    public double preUntransformX(double x) {
        x /= this.scaleX;
        return x - this.m03;
    }

    public double preUntransformY(double y) {
        y /= this.scaleY;
        return y - this.m13;
    }

    public double preUntransformZ(double z) {
        z /= this.scaleZ;
        return z - this.m23;
    }

    public void rotateX(float deg) {
        this.rotateXRad(deg * Mth.DEG_TO_RAD);
    }

    public void rotateXRad(float rad) {
        double sin = Mth.sin(rad);
        double cos = Mth.cos(rad);
        double f1 = this.m01 * cos + this.m02 * sin;   //0          1    0    0    0
        double f2 = this.m01 * -sin + this.m02 * cos;  //0          0   cos  sin   0
        double f5 = this.m11 * cos + this.m12 * sin;   //cos        0  -sin  cos   0
        double f6 = this.m11 * -sin + this.m12 * cos;  //-sin       0    0    0    1
        double f9 = this.m21 * cos + this.m22 * sin;   //sin
        double f10 = this.m21 * -sin + this.m22 * cos; //cos
        this.m01 = f1;
        this.m02 = f2;
        this.m11 = f5;
        this.m12 = f6;
        this.m21 = f9;
        this.m22 = f10;
    }

    public void rotateY(float deg) {
        this.rotateYRad(deg * Mth.DEG_TO_RAD);
    }

    public void rotateYRad(float rad) {
        double sin = Mth.sin(rad);
        double cos = Mth.cos(rad);
        double f0 = this.m00 * cos + this.m02 * -sin;  //cos       cos   0  -sin   0
        double f2 = this.m00 * sin + this.m02 * cos;   //sin        0    1    0    0
        double f4 = this.m10 * cos + this.m12 * -sin;  //0         sin   0   cos   0
        double f6 = this.m10 * sin + this.m12 * cos;   //0          0    0    0    1
        double f8 = this.m20 * cos + this.m22 * -sin;  //-sin
        double f10 = this.m20 * sin + this.m22 * cos;  //cos
        this.m00 = f0;
        this.m02 = f2;
        this.m10 = f4;
        this.m12 = f6;
        this.m20 = f8;
        this.m22 = f10;
    }

    public void rotateZ(float deg) {
        this.rotateZRad(deg * Mth.DEG_TO_RAD);
    }

    public void rotateZRad(float rad) {
        double sin = Mth.sin(rad);
        double cos = Mth.cos(rad);
        double f0 = this.m00 * cos + this.m01 * sin;  //cos       cos  sin   0    0
        double f1 = this.m00 * -sin + this.m01 * cos; //-sin     -sin  cos   0    0
        double f4 = this.m10 * cos + this.m11 * sin;  //sin        0    0    1    0
        double f5 = this.m10 * -sin + this.m11 * cos; //cos        0    0    0    1
        double f8 = this.m20 * cos + this.m21 * sin;  //0
        double f9 = this.m20 * -sin + this.m21 * cos; //0
        this.m00 = f0;
        this.m01 = f1;
        this.m10 = f4;
        this.m11 = f5;
        this.m20 = f8;
        this.m21 = f9;
    }

    public void scale(double x, double y, double z) {
        this.scaleX *= x;
        this.scaleY *= y;
        this.scaleZ *= z;
    }

    public Matrix4d set(Matrix4d matrix) {
        this.m00 = matrix.m00;
        this.m01 = matrix.m01;
        this.m02 = matrix.m02;
        this.m03 = matrix.m03;
        this.m10 = matrix.m10;
        this.m11 = matrix.m11;
        this.m12 = matrix.m12;
        this.m13 = matrix.m13;
        this.m20 = matrix.m20;
        this.m21 = matrix.m21;
        this.m22 = matrix.m22;
        this.m23 = matrix.m23;
        this.scaleX = matrix.scaleX;
        this.scaleY = matrix.scaleY;
        this.scaleZ = matrix.scaleZ;
        return this;
    }

    public Matrix4d setIdentity() {
        this.m00 = this.m11 = this.m22 = this.scaleX = this.scaleY = this.scaleZ = 1;
        this.m01 = this.m02 = this.m03 = this.m10 = this.m12 = this.m13 = this.m20 = this.m21 = this.m23 = 0;
        return this;
    }

    public Vec3d transform(Vec3d vec) {
        return vec.set(this.transformX(vec.x, vec.y, vec.z), this.transformY(vec.x, vec.y, vec.z), this.transformZ(vec.x, vec.y, vec.z));
    }

    public double transformX(double x, double y, double z) {
        return (x * this.m00 + y * this.m01 + z * this.m02 + this.m03) * this.scaleX;
    }

    public double transformY(double x, double y, double z) {
        return (x * this.m10 + y * this.m11 + z * this.m12 + this.m13) * this.scaleY;
    }

    public double transformZ(double x, double y, double z) {
        return (x * this.m20 + y * this.m21 + z * this.m22 + this.m23) * this.scaleZ;
    }

    public void translate(double x, double y, double z) {
        this.m03 += this.m00 * x + this.m01 * y + this.m02 * z;
        this.m13 += this.m10 * x + this.m11 * y + this.m12 * z;
        this.m23 += this.m20 * x + this.m21 * y + this.m22 * z;
    }

    public Vec3d untransform(Vec3d vec) {
        vec.divideMutable(this.scaleX, this.scaleY, this.scaleZ).subMutable(this.m03, this.m13, this.m23);
        double x = vec.x;
        double y = vec.y;
        double z = vec.z;
        return vec.set(this.postUntransformX(x, y, z), this.postUntransformY(x, y, z), this.postUntransformZ(x, y, z));
    }
}
