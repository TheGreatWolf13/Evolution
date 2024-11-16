package tgw.evolution.mixin;

import com.mojang.math.Matrix4f;
import com.mojang.math.Vector4f;
import net.minecraft.client.renderer.culling.Frustum;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import tgw.evolution.client.renderer.chunk.Visibility;
import tgw.evolution.patches.PatchFrustum;

@Mixin(Frustum.class)
public abstract class MixinFrustum implements PatchFrustum {

    @Shadow private double camX;
    @Shadow private double camY;
    @Shadow private double camZ;
    @Shadow @Final private Vector4f[] frustumData;
    @Shadow private Vector4f viewVector;

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations.
     */
    @Overwrite
    public final void calculateFrustum(Matrix4f proj, Matrix4f frustum) {
        frustum.multiply(proj);
        frustum.transpose();
        this.viewVector.set(0, 0, 1, 0);
        this.viewVector.transform(frustum);
        this.getPlane(frustum, -1, 0, 0, 0);
        this.getPlane(frustum, 1, 0, 0, 1);
        this.getPlane(frustum, 0, -1, 0, 2);
        this.getPlane(frustum, 0, 1, 0, 3);
        this.getPlane(frustum, 0, 0, -1, 4);
        this.getPlane(frustum, 0, 0, 1, 5);
    }

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations
     */
    @Overwrite
    private boolean cubeCompletelyInFrustum(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        for (Vector4f plane : this.frustumData) {
            float x = plane.x();
            float y = plane.y();
            float z = plane.z();
            if (x * (x < 0 ? maxX : minX) + y * (y < 0 ? maxY : minY) + z * (z < 0 ? maxZ : minZ) + plane.w() < 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations.
     */
    @Overwrite
    private boolean cubeInFrustum(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        for (Vector4f plane : this.frustumData) {
            float x = plane.x();
            float y = plane.y();
            float z = plane.z();
            if (x * (x < 0 ? minX : maxX) + y * (y < 0 ? minY : maxY) + z * (z < 0 ? minZ : maxZ) < -plane.w()) {
                return false;
            }
        }
        return true;
    }

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations.
     */
    @Overwrite
    private void getPlane(Matrix4f matrix, int x, int y, int z, int index) {
        Vector4f vec = this.frustumData[index];
        vec.set(x, y, z, 1);
        vec.transform(matrix);
        vec.normalize();
    }

    @Unique
    private @Visibility int intersect(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        boolean inside = true;
        for (Vector4f plane : this.frustumData) {
            float x = plane.x();
            float y = plane.y();
            float z = plane.z();
            float x0;
            float x1;
            if (x < 0) {
                x0 = minX;
                x1 = maxX;
            }
            else {
                x0 = maxX;
                x1 = minX;
            }
            float y0;
            float y1;
            if (y < 0) {
                y0 = minY;
                y1 = maxY;
            }
            else {
                y0 = maxY;
                y1 = minY;
            }
            float z0;
            float z1;
            if (z < 0) {
                z0 = minZ;
                z1 = maxZ;
            }
            else {
                z0 = maxZ;
                z1 = minZ;
            }
            if (x * x0 + y * y0 + z * z0 + plane.w() >= 0) {
                inside &= x * x1 + y * y1 + z * z1 + plane.w() >= 0;
            }
            else {
                return Visibility.OUTSIDE;
            }
        }
        return inside ? Visibility.INSIDE : Visibility.INTERSECT;
    }

    @Override
    public @Visibility int intersectWith(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        return this.intersect((float) (minX - this.camX), (float) (minY - this.camY), (float) (minZ - this.camZ), (float) (maxX - this.camX), (float) (maxY - this.camY), (float) (maxZ - this.camZ));
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public Frustum offsetToFullyIncludeCameraCube(int offset) {
        double x0 = Math.floor(this.camX / offset) * offset;
        double y0 = Math.floor(this.camY / offset) * offset;
        double z0 = Math.floor(this.camZ / offset) * offset;
        double x1 = Math.ceil(this.camX / offset) * offset;
        double y1 = Math.ceil(this.camY / offset) * offset;
        double z1 = Math.ceil(this.camZ / offset) * offset;
        while (!this.cubeCompletelyInFrustum((float) (x0 - this.camX), (float) (y0 - this.camY), (float) (z0 - this.camZ), (float) (x1 - this.camX), (float) (y1 - this.camY), (float) (z1 - this.camZ))) {
            this.camX -= this.viewVector.x() * 4.0F;
            this.camY -= this.viewVector.y() * 4.0F;
            this.camZ -= this.viewVector.z() * 4.0F;
        }
        return (Frustum) (Object) this;
    }

    @Redirect(method = "<init>(Lcom/mojang/math/Matrix4f;Lcom/mojang/math/Matrix4f;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client" +
                                                                                                                            "/renderer/culling" +
                                                                                                                            "/Frustum;" +
                                                                                                                            "calculateFrustum" +
                                                                                                                            "(Lcom/mojang/math" +
                                                                                                                            "/Matrix4f;" +
                                                                                                                            "Lcom/mojang/math" +
                                                                                                                            "/Matrix4f;)V"))
    private void onInit(Frustum instance, Matrix4f projection, Matrix4f frustrumMatrix) {
        this.viewVector = new Vector4f();
        for (int i = 0; i < 6; ++i) {
            //noinspection ObjectAllocationInLoop
            this.frustumData[i] = new Vector4f();
        }
        this.calculateFrustum(projection, frustrumMatrix);
    }
}
