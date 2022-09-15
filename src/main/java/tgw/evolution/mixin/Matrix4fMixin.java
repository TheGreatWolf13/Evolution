package tgw.evolution.mixin;

import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.patches.IMatrix4fPatch;

@Mixin(Matrix4f.class)
public abstract class Matrix4fMixin implements IMatrix4fPatch {

    @Shadow
    protected float m00;
    @Shadow
    protected float m01;
    @Shadow
    protected float m02;
    @Shadow
    protected float m03;
    @Shadow
    protected float m10;
    @Shadow
    protected float m11;
    @Shadow
    protected float m12;
    @Shadow
    protected float m13;
    @Shadow
    protected float m20;
    @Shadow
    protected float m21;
    @Shadow
    protected float m22;
    @Shadow
    protected float m23;
    @Shadow
    protected float m30;
    @Shadow
    protected float m31;
    @Shadow
    protected float m32;

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations and use faster, specialized functions
     */
    @Overwrite
    public void multiply(Quaternion quaternion) {
        this.rotate(quaternion);
    }

    @Override
    public void rotate(Quaternion quaternion) {
        boolean i = quaternion.i() != 0.0F;
        boolean j = quaternion.j() != 0.0F;
        boolean k = quaternion.k() != 0.0F;
        // Try to determine if this is a simple rotation on one axis component only
        if (i) {
            if (!j && !k) {
                this.rotateX(quaternion.i(), quaternion.r());
            }
            else {
                this.rotateXYZ(quaternion);
            }
        }
        else if (j) {
            if (!k) {
                this.rotateY(quaternion.j(), quaternion.r());
            }
            else {
                this.rotateXYZ(quaternion);
            }
        }
        else if (k) {
            this.rotateZ(quaternion.k(), quaternion.r());
        }
    }

    @Override
    public void rotateX(float i, float r) {
        float ii = 2.0F * i * i;
        float ta11 = 1.0F - ii;
        float ta22 = 1.0F - ii;
        float ir = i * r;
        float ta21 = 2.0F * ir;
        float ta12 = 2.0F * -ir;
        float m01 = this.m01 * ta11 + this.m02 * ta21;
        float m02 = this.m01 * ta12 + this.m02 * ta22;
        float m11 = this.m11 * ta11 + this.m12 * ta21;
        float m12 = this.m11 * ta12 + this.m12 * ta22;
        float m21 = this.m21 * ta11 + this.m22 * ta21;
        float m22 = this.m21 * ta12 + this.m22 * ta22;
        float m31 = this.m31 * ta11 + this.m32 * ta21;
        float a32 = this.m31 * ta12 + this.m32 * ta22;
        this.m01 = m01;
        this.m02 = m02;
        this.m11 = m11;
        this.m12 = m12;
        this.m21 = m21;
        this.m22 = m22;
        this.m31 = m31;
        this.m32 = a32;
    }

    private void rotateXYZ(Quaternion quaternion) {
        float i = quaternion.i();
        float j = quaternion.j();
        float k = quaternion.k();
        float r = quaternion.r();
        float ii = 2.0F * i * i;
        float jj = 2.0F * j * j;
        float kk = 2.0F * k * k;
        float ta00 = 1.0F - jj - kk;
        float ta11 = 1.0F - kk - ii;
        float ta22 = 1.0F - ii - jj;
        float ij = i * j;
        float jk = j * k;
        float ki = k * i;
        float ir = i * r;
        float jr = j * r;
        float kr = k * r;
        float ta10 = 2.0F * (ij + kr);
        float ta01 = 2.0F * (ij - kr);
        float ta20 = 2.0F * (ki - jr);
        float ta02 = 2.0F * (ki + jr);
        float ta21 = 2.0F * (jk + ir);
        float ta12 = 2.0F * (jk - ir);
        float m00 = this.m00 * ta00 + this.m01 * ta10 + this.m02 * ta20;
        float m01 = this.m00 * ta01 + this.m01 * ta11 + this.m02 * ta21;
        float m02 = this.m00 * ta02 + this.m01 * ta12 + this.m02 * ta22;
        float m10 = this.m10 * ta00 + this.m11 * ta10 + this.m12 * ta20;
        float m11 = this.m10 * ta01 + this.m11 * ta11 + this.m12 * ta21;
        float m12 = this.m10 * ta02 + this.m11 * ta12 + this.m12 * ta22;
        float m20 = this.m20 * ta00 + this.m21 * ta10 + this.m22 * ta20;
        float m21 = this.m20 * ta01 + this.m21 * ta11 + this.m22 * ta21;
        float m22 = this.m20 * ta02 + this.m21 * ta12 + this.m22 * ta22;
        float m30 = this.m30 * ta00 + this.m31 * ta10 + this.m32 * ta20;
        float m31 = this.m30 * ta01 + this.m31 * ta11 + this.m32 * ta21;
        float m32 = this.m30 * ta02 + this.m31 * ta12 + this.m32 * ta22;
        this.m00 = m00;
        this.m01 = m01;
        this.m02 = m02;
        this.m10 = m10;
        this.m11 = m11;
        this.m12 = m12;
        this.m20 = m20;
        this.m21 = m21;
        this.m22 = m22;
        this.m30 = m30;
        this.m31 = m31;
        this.m32 = m32;
    }

    @Override
    public void rotateY(float j, float r) {
        float jj = 2.0F * j * j;
        float ta00 = 1.0F - jj;
        float ta22 = 1.0F - jj;
        float jr = j * r;
        float ta20 = 2.0F * -jr;
        float ta02 = 2.0F * jr;
        float m00 = this.m00 * ta00 + this.m02 * ta20;
        float m02 = this.m00 * ta02 + this.m02 * ta22;
        float m10 = this.m10 * ta00 + this.m12 * ta20;
        float m12 = this.m10 * ta02 + this.m12 * ta22;
        float m20 = this.m20 * ta00 + this.m22 * ta20;
        float m22 = this.m20 * ta02 + this.m22 * ta22;
        float m30 = this.m30 * ta00 + this.m32 * ta20;
        float m32 = this.m30 * ta02 + this.m32 * ta22;
        this.m00 = m00;
        this.m02 = m02;
        this.m10 = m10;
        this.m12 = m12;
        this.m20 = m20;
        this.m22 = m22;
        this.m30 = m30;
        this.m32 = m32;
    }

    @Override
    public void rotateZ(float k, float r) {
        float kk = 2.0F * k * k;
        float ta00 = 1.0F - kk;
        float ta11 = 1.0F - kk;
        float kr = k * r;
        float ta10 = 2.0F * kr;
        float ta01 = 2.0F * -kr;
        float m00 = this.m00 * ta00 + this.m01 * ta10;
        float m01 = this.m00 * ta01 + this.m01 * ta11;
        float m10 = this.m10 * ta00 + this.m11 * ta10;
        float m11 = this.m10 * ta01 + this.m11 * ta11;
        float m20 = this.m20 * ta00 + this.m21 * ta10;
        float m21 = this.m20 * ta01 + this.m21 * ta11;
        float m30 = this.m30 * ta00 + this.m31 * ta10;
        float m31 = this.m30 * ta01 + this.m31 * ta11;
        this.m00 = m00;
        this.m01 = m01;
        this.m10 = m10;
        this.m11 = m11;
        this.m20 = m20;
        this.m21 = m21;
        this.m30 = m30;
        this.m31 = m31;
    }

    @Override
    public void scale(float x, float y, float z) {
        this.m00 *= x;
        this.m01 *= y;
        this.m02 *= z;
        this.m10 *= x;
        this.m11 *= y;
        this.m12 *= z;
        this.m20 *= x;
        this.m21 *= y;
        this.m22 *= z;
        this.m30 *= x;
        this.m31 *= y;
        this.m32 *= z;
    }

    @Override
    public float transformVecX(float x, float y, float z) {
        return this.m00 * x + this.m01 * y + this.m02 * z + this.m03;
    }

    @Override
    public float transformVecY(float x, float y, float z) {
        return this.m10 * x + this.m11 * y + this.m12 * z + this.m13;
    }

    @Override
    public float transformVecZ(float x, float y, float z) {
        return this.m20 * x + this.m21 * y + this.m22 * z + this.m23;
    }
}
