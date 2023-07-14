package tgw.evolution.mixin;

import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.patches.PatchQuaternion;

@Mixin(Quaternion.class)
public abstract class MixinQuaternion implements PatchQuaternion {

    @Shadow private float i;
    @Shadow private float j;
    @Shadow private float k;
    @Shadow private float r;

    @Shadow
    private static float cos(float pAngle) {
        throw new AbstractMethodError();
    }

    @Shadow
    private static float sin(float pAngle) {
        throw new AbstractMethodError();
    }

    @Override
    public void mul(Vector3f vec, float angleRad) {
        angleRad *= 0.5f;
        float s = sin(angleRad);
        float i = vec.x() * s;
        float j = vec.y() * s;
        float k = vec.z() * s;
        float r = cos(angleRad);
        //Mul
        float a = this.i;
        float b = this.j;
        float c = this.k;
        float d = this.r;
        this.i = d * i + a * r + b * k - c * j;
        this.j = d * j - a * k + b * r + c * i;
        this.k = d * k + a * j - b * i + c * r;
        this.r = d * r - a * i - b * j - c * k;
    }

    @Override
    public Quaternion set(Vector3f axis, float angle, boolean degrees) {
        if (degrees) {
            angle = Mth.DEG_TO_RAD * angle;
        }
        angle *= 0.5f;
        float f = sin(angle);
        this.i = axis.x() * f;
        this.j = axis.y() * f;
        this.k = axis.z() * f;
        this.r = cos(angle);
        return (Quaternion) (Object) this;
    }
}
