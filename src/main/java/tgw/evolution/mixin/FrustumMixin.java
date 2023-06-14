package tgw.evolution.mixin;

import com.mojang.math.Vector4f;
import net.minecraft.client.renderer.culling.Frustum;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.util.math.VectorUtil;

@Mixin(Frustum.class)
public abstract class FrustumMixin {

    @Shadow
    @Final
    private Vector4f[] frustumData;

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations
     */
    @Overwrite
    private boolean cubeCompletelyInFrustum(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        for (int i = 0; i < 6; ++i) {
            Vector4f vec = this.frustumData[i];
            if (VectorUtil.dot(vec, minX, minY, minZ) <= 0.0F) {
                return false;
            }
            if (VectorUtil.dot(vec, maxX, minY, minZ) <= 0.0F) {
                return false;
            }
            if (VectorUtil.dot(vec, minX, maxY, minZ) <= 0.0F) {
                return false;
            }
            if (VectorUtil.dot(vec, maxX, maxY, minZ) <= 0.0F) {
                return false;
            }
            if (VectorUtil.dot(vec, minX, minY, maxZ) <= 0.0F) {
                return false;
            }
            if (VectorUtil.dot(vec, maxX, minY, maxZ) <= 0.0F) {
                return false;
            }
            if (VectorUtil.dot(vec, minX, maxY, maxZ) <= 0.0F) {
                return false;
            }
            if (VectorUtil.dot(vec, maxX, maxY, maxZ) <= 0.0F) {
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
    public boolean cubeInFrustum(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        for (int i = 0; i < 6; ++i) {
            Vector4f vec = this.frustumData[i];
            if (VectorUtil.dot(vec, minX, minY, minZ) <= 0.0F &&
                VectorUtil.dot(vec, maxX, minY, minZ) <= 0.0F &&
                VectorUtil.dot(vec, minX, maxY, minZ) <= 0.0F &&
                VectorUtil.dot(vec, maxX, maxY, minZ) <= 0.0F &&
                VectorUtil.dot(vec, minX, minY, maxZ) <= 0.0F &&
                VectorUtil.dot(vec, maxX, minY, maxZ) <= 0.0F &&
                VectorUtil.dot(vec, minX, maxY, maxZ) <= 0.0F &&
                VectorUtil.dot(vec, maxX, maxY, maxZ) <= 0.0F) {
                return false;
            }
        }
        return true;
    }
}
