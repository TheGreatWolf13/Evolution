package tgw.evolution.mixin;

import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.patches.IQuaternionPatch;
import tgw.evolution.util.math.MathHelper;

@Mixin(Quaternion.class)
public abstract class QuaternionMixin implements IQuaternionPatch {

    @Shadow
    private float i;
    @Shadow
    private float j;
    @Shadow
    private float k;
    @Shadow
    private float r;

    @Shadow
    private static float cos(float pAngle) {
        throw new AbstractMethodError();
    }

    @Shadow
    private static float sin(float pAngle) {
        throw new AbstractMethodError();
    }

    @Override
    public void set(Vector3f axis, float angle, boolean degrees) {
        if (degrees) {
            angle = MathHelper.degToRad(angle);
        }
        angle /= 2.0f;
        float f = sin(angle);
        this.i = axis.x() * f;
        this.j = axis.y() * f;
        this.k = axis.z() * f;
        this.r = cos(angle);
    }
}
