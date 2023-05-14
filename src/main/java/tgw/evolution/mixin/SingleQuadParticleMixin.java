package tgw.evolution.mixin;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import tgw.evolution.util.math.ColorABGR;

@Mixin(SingleQuadParticle.class)
public abstract class SingleQuadParticleMixin extends Particle {

    public SingleQuadParticleMixin(ClientLevel pLevel, double pX, double pY, double pZ) {
        super(pLevel, pX, pY, pZ);
    }

    @Unique
    private static void addVertex(VertexConsumer buffer,
                                  Quaternion rotation,
                                  float x,
                                  float y,
                                  float posX,
                                  float posY,
                                  float posZ,
                                  float u,
                                  float v,
                                  int color,
                                  int light,
                                  float size) {
        // Quaternion q0 = new Quaternion(rotation);
        float q0x = rotation.i();
        float q0y = rotation.j();
        float q0z = rotation.k();
        float q0w = rotation.r();
        // q0.hamiltonProduct(x, y, 0.0f, 0.0f)
        float q1x = q0w * x - q0z * y;
        float q1y = q0w * y + q0z * x;
        float q1w = q0x * y - q0y * x;
        float q1z = -q0x * x - q0y * y;
        // Quaternion q2 = new Quaternion(rotation);
        // q2.conjugate()
        float q2x = -q0x;
        float q2y = -q0y;
        float q2z = -q0z;
        // q2.hamiltonProduct(q1)
        float q3x = q1z * q2x + q1x * q0w + q1y * q2z - q1w * q2y;
        float q3y = q1z * q2y - q1x * q2z + q1y * q0w + q1w * q2x;
        float q3z = q1z * q2z + q1x * q2y - q1y * q2x + q1w * q0w;
        // Vector3f f = new Vector3f(q2.getX(), q2.getY(), q2.getZ())
        // f.multiply(size)
        // f.add(pos)
        float fx = q3x * size + posX;
        float fy = q3y * size + posY;
        float fz = q3z * size + posZ;
        buffer.vertex(fx, fy, fz)
              .uv(u, v)
              .color(color)
              .uv2(light)
              .endVertex();
    }

    @Shadow
    public abstract float getQuadSize(float pScaleFactor);

    @Shadow
    protected abstract float getU0();

    @Shadow
    protected abstract float getU1();

    @Shadow
    protected abstract float getV0();

    @Shadow
    protected abstract float getV1();

    /**
     * @author JellySquid
     * @reason Optimize function
     */
    @Override
    @Overwrite
    public void render(VertexConsumer vertexConsumer, Camera camera, float tickDelta) {
        Vec3 vec3d = camera.getPosition();
        float x = (float) (Mth.lerp(tickDelta, this.xo, this.x) - vec3d.x());
        float y = (float) (Mth.lerp(tickDelta, this.yo, this.y) - vec3d.y());
        float z = (float) (Mth.lerp(tickDelta, this.zo, this.z) - vec3d.z());
        Quaternion quaternion;
        if (this.roll == 0.0F) {
            quaternion = camera.rotation();
        }
        else {
            float angle = Mth.lerp(tickDelta, this.oRoll, this.roll);
            quaternion = new Quaternion(camera.rotation());
            quaternion.mul(Vector3f.ZP.rotation(angle));
        }
        float size = this.getQuadSize(tickDelta);
        int light = this.getLightColor(tickDelta);
        float minU = this.getU0();
        float maxU = this.getU1();
        float minV = this.getV0();
        float maxV = this.getV1();
        int color = ColorABGR.pack(this.rCol, this.gCol, this.bCol, this.alpha);
        addVertex(vertexConsumer, quaternion, -1.0F, -1.0F, x, y, z, maxU, maxV, color, light, size);
        addVertex(vertexConsumer, quaternion, -1.0F, 1.0F, x, y, z, maxU, minV, color, light, size);
        addVertex(vertexConsumer, quaternion, 1.0F, 1.0F, x, y, z, minU, minV, color, light, size);
        addVertex(vertexConsumer, quaternion, 1.0F, -1.0F, x, y, z, minU, maxV, color, light, size);
    }
}
