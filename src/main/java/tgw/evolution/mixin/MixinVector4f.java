package tgw.evolution.mixin;

import com.mojang.math.Quaternion;
import com.mojang.math.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Vector4f.class)
public abstract class MixinVector4f {

    @Shadow private float w;
    @Shadow private float x;
    @Shadow private float y;
    @Shadow private float z;

    @Shadow
    public abstract void set(float pX, float pY, float pZ, float pW);

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations
     */
    @Overwrite
    public void transform(Quaternion quaternion) {
        //First param
        float i = quaternion.i();
        float j = quaternion.j();
        float k = quaternion.k();
        float r = quaternion.r();
        //Perform multiplication
        float i_ = i;
        float j_ = j;
        float k_ = k;
        float r_ = r;
        i = r_ * this.x + j_ * this.z - k_ * this.y;
        j = r_ * this.y - i_ * this.z + k_ * this.x;
        k = r_ * this.z + i_ * this.y - j_ * this.x;
        r = -i_ * this.x - j_ * this.y - k_ * this.z;
        //Second param
        float i2 = -quaternion.i();
        float j2 = -quaternion.j();
        float k2 = -quaternion.k();
        float r2 = quaternion.r();
        //Perform multiplication
        i_ = i;
        j_ = j;
        k_ = k;
        i = r * i2 + i_ * r2 + j_ * k2 - k_ * j2;
        j = r * j2 - i_ * k2 + j_ * r2 + k_ * i2;
        k = r * k2 + i_ * j2 - j_ * i2 + k_ * r2;
        this.set(i, j, k, this.w);
    }
}
