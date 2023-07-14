package tgw.evolution.mixin;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import tgw.evolution.util.constants.CommonRotations;

@Mixin(GlStateManager.class)
public abstract class MixinGlStateManager {

    @Unique private static final Matrix4f MATRIX_FLAT = new Matrix4f();
    @Unique private static final Matrix4f MATRIX_3D = new Matrix4f();

    static {
        MATRIX_FLAT.setIdentity();
        MATRIX_FLAT.scale(1.0F, -1.0F, 1.0F);
        MATRIX_FLAT.multiply(CommonRotations.YN22_5);
        MATRIX_FLAT.multiply(CommonRotations.XP135);
        MATRIX_3D.setIdentity();
        MATRIX_3D.multiply(CommonRotations.YP62);
        MATRIX_3D.multiply(CommonRotations.XP185_5);
        MATRIX_3D.multiply(CommonRotations.YN22_5);
        MATRIX_3D.multiply(CommonRotations.XP135);
    }

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations, cache the matrix
     */
    @Overwrite
    public static void setupGui3DDiffuseLighting(Vector3f lightingVector1, Vector3f lightingVector2) {
        setupLevelDiffuseLighting(lightingVector1, lightingVector2, MATRIX_3D);
    }

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations, cache the matrix
     */
    @Overwrite
    public static void setupGuiFlatDiffuseLighting(Vector3f lighting1, Vector3f lighting2) {
        setupLevelDiffuseLighting(lighting1, lighting2, MATRIX_FLAT);
    }

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations
     */
    @Overwrite
    public static void setupLevelDiffuseLighting(Vector3f lightingVector1, Vector3f lightingVector2, Matrix4f matrix) {
        RenderSystem.assertOnRenderThread();
        float x0 = matrix.transformVecX(lightingVector1.x(), lightingVector1.y(), lightingVector1.z());
        float y0 = matrix.transformVecY(lightingVector1.x(), lightingVector1.y(), lightingVector1.z());
        float z0 = matrix.transformVecZ(lightingVector1.x(), lightingVector1.y(), lightingVector1.z());
        float x1 = matrix.transformVecX(lightingVector2.x(), lightingVector2.y(), lightingVector2.z());
        float y1 = matrix.transformVecY(lightingVector2.x(), lightingVector2.y(), lightingVector2.z());
        float z1 = matrix.transformVecZ(lightingVector2.x(), lightingVector2.y(), lightingVector2.z());
        RenderSystem.shaderLightDirections[0].set(x0, y0, z0);
        RenderSystem.shaderLightDirections[1].set(x1, y1, z1);
    }
}
