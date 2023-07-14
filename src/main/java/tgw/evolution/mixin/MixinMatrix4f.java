package tgw.evolution.mixin;

import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.patches.PatchMatrix4f;

@Mixin(Matrix4f.class)
public abstract class MixinMatrix4f implements PatchMatrix4f {

    @Shadow protected float m00;
    @Shadow protected float m01;
    @Shadow protected float m02;
    @Shadow protected float m03;
    @Shadow protected float m10;
    @Shadow protected float m11;
    @Shadow protected float m12;
    @Shadow protected float m13;
    @Shadow protected float m20;
    @Shadow protected float m21;
    @Shadow protected float m22;
    @Shadow protected float m23;
    @Shadow protected float m30;
    @Shadow protected float m31;
    @Shadow protected float m32;
    @Shadow protected float m33;

    @Shadow
    public abstract void load(Matrix4f matrix4f);

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations and use faster, specialized functions
     */
    @Overwrite
    public void multiply(Quaternion quaternion) {
        this.rotate(quaternion);
    }

    @Override
    public void multiplyWithPerspective(double fov, float aspectRatio, float nearPlane, float farPlane) {
        //Construct perspective
        float d = (float) (1.0 / Math.tan(fov * (Math.PI / 360)));
        float o00 = d / aspectRatio;
        float o22 = (farPlane + nearPlane) / (nearPlane - farPlane);
        float o32 = -1.0F;
        float o23 = 2.0F * farPlane * nearPlane / (nearPlane - farPlane);
        //Multiply
        this.m00 *= o00;
        this.m10 *= o00;
        this.m20 *= o00;
        this.m30 *= o00;
        this.m01 *= d;
        this.m11 *= d;
        this.m21 *= d;
        this.m31 *= d;
        float m02 = this.m02 * o22 + this.m03 * o32;
        this.m03 = this.m02 * o23;
        this.m02 = m02;
        float m12 = this.m12 * o22 + this.m13 * o32;
        this.m13 = this.m12 * o23;
        this.m12 = m12;
        float m22 = this.m22 * o22 + this.m23 * o32;
        this.m23 = this.m22 * o23;
        this.m22 = m22;
        float m32 = this.m32 * o22 + this.m33 * o32;
        this.m33 = this.m32 * o23;
        this.m32 = m32;
    }

    @Override
    public void rotate(float i, float j, float k, float r) {
        boolean hasI = i != 0.0F;
        boolean hasJ = j != 0.0F;
        boolean hasK = k != 0.0F;
        // Try to determine if this is a simple rotation on one axis component only
        if (hasI) {
            if (!hasJ && !hasK) {
                this.rotateX(i, r);
            }
            else {
                this.rotateXYZ(i, j, k, r);
            }
        }
        else if (hasJ) {
            if (!hasK) {
                this.rotateY(j, r);
            }
            else {
                this.rotateXYZ(i, j, k, r);
            }
        }
        else if (hasK) {
            this.rotateZ(k, r);
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
        this.m02 = this.m01 * ta12 + this.m02 * ta22;
        this.m01 = m01;
        float m11 = this.m11 * ta11 + this.m12 * ta21;
        this.m12 = this.m11 * ta12 + this.m12 * ta22;
        this.m11 = m11;
        float m21 = this.m21 * ta11 + this.m22 * ta21;
        this.m22 = this.m21 * ta12 + this.m22 * ta22;
        this.m21 = m21;
        float m31 = this.m31 * ta11 + this.m32 * ta21;
        this.m32 = this.m31 * ta12 + this.m32 * ta22;
        this.m31 = m31;
    }

    private void rotateXYZ(float i, float j, float k, float r) {
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
        this.m02 = this.m00 * ta02 + this.m01 * ta12 + this.m02 * ta22;
        this.m01 = m01;
        this.m00 = m00;
        float m10 = this.m10 * ta00 + this.m11 * ta10 + this.m12 * ta20;
        float m11 = this.m10 * ta01 + this.m11 * ta11 + this.m12 * ta21;
        this.m12 = this.m10 * ta02 + this.m11 * ta12 + this.m12 * ta22;
        this.m11 = m11;
        this.m10 = m10;
        float m20 = this.m20 * ta00 + this.m21 * ta10 + this.m22 * ta20;
        float m21 = this.m20 * ta01 + this.m21 * ta11 + this.m22 * ta21;
        this.m22 = this.m20 * ta02 + this.m21 * ta12 + this.m22 * ta22;
        this.m21 = m21;
        this.m20 = m20;
        float m30 = this.m30 * ta00 + this.m31 * ta10 + this.m32 * ta20;
        float m31 = this.m30 * ta01 + this.m31 * ta11 + this.m32 * ta21;
        this.m32 = this.m30 * ta02 + this.m31 * ta12 + this.m32 * ta22;
        this.m31 = m31;
        this.m30 = m30;
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
        this.m02 = this.m00 * ta02 + this.m02 * ta22;
        this.m00 = m00;
        float m10 = this.m10 * ta00 + this.m12 * ta20;
        this.m12 = this.m10 * ta02 + this.m12 * ta22;
        this.m10 = m10;
        float m20 = this.m20 * ta00 + this.m22 * ta20;
        this.m22 = this.m20 * ta02 + this.m22 * ta22;
        this.m20 = m20;
        float m30 = this.m30 * ta00 + this.m32 * ta20;
        this.m32 = this.m30 * ta02 + this.m32 * ta22;
        this.m30 = m30;
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
        this.m01 = this.m00 * ta01 + this.m01 * ta11;
        this.m00 = m00;
        float m10 = this.m10 * ta00 + this.m11 * ta10;
        this.m11 = this.m10 * ta01 + this.m11 * ta11;
        this.m10 = m10;
        float m20 = this.m20 * ta00 + this.m21 * ta10;
        this.m21 = this.m20 * ta01 + this.m21 * ta11;
        this.m20 = m20;
        float m30 = this.m30 * ta00 + this.m31 * ta10;
        this.m31 = this.m30 * ta01 + this.m31 * ta11;
        this.m30 = m30;
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
