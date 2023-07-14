package tgw.evolution.mixin;

import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Vector3f.class)
public abstract class MixinVector3f {

    @Shadow private float x;
    @Shadow private float y;
    @Shadow private float z;

    @Shadow
    public abstract void set(float pX, float pY, float pZ);

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations
     */
    @Overwrite
    public void transform(Quaternion quat) {
        //Quaternion quaternion = new Quaternion(quat)
        float qi = quat.i();
        float qj = quat.j();
        float qk = quat.k();
        float qr = quat.r();
        //Quaternion quatThis = new Quaternion(this.x, this.y, this.z, 0.0f)
        float ti = this.x;
        float tj = this.y;
        float tk = this.z;
        //quaternion.mul(quatThis)
        float fi = qi;
        float fj = qj;
        float fk = qk;
        float fr = qr;
        qi = fr * ti + fj * tk - fk * tj;
        qj = fr * tj - fi * tk + fk * ti;
        qk = fr * tk + fi * tj - fj * ti;
        qr = -fi * ti - fj * tj - fk * tk;
        //conj = new Quaternion(quat).conj()
        float ci = -fi;
        float cj = -fj;
        float ck = -fk;
        //quaternion.mul(conj)
        float f0 = qi;
        float f1 = qj;
        float f2 = qk;
        float f3 = qr;
        qi = f3 * ci + f0 * fr + f1 * ck - f2 * cj;
        qj = f3 * cj - f0 * ck + f1 * fr + f2 * ci;
        qk = f3 * ck + f0 * cj - f1 * ci + f2 * fr;
        //
        this.set(qi, qj, qk);
    }
}
