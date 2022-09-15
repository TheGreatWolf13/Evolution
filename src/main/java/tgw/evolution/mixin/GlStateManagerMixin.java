package tgw.evolution.mixin;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tgw.evolution.patches.IMatrix4fPatch;
import tgw.evolution.util.constants.CommonRotations;
import tgw.evolution.util.math.MathHelper;

@Mixin(GlStateManager.class)
public abstract class GlStateManagerMixin {

    /**
     * @author TheGreatWolf
     * @reason Avoid most allocations
     */
    @Overwrite
    public static void setupGui3DDiffuseLighting(Vector3f lightingVector1, Vector3f lightingVector2) {
        RenderSystem.assertOnRenderThread();
        Matrix4f matrix = new Matrix4f();
        matrix.setIdentity();
        matrix.multiply(CommonRotations.YP62);
        matrix.multiply(CommonRotations.XP185_5);
        matrix.multiply(CommonRotations.YN22_5);
        matrix.multiply(CommonRotations.XP135);
        setupLevelDiffuseLighting(lightingVector1, lightingVector2, matrix);
    }

    /**
     * @author TheGreatWolf
     * @reason Avoid most allocations and use faster, specialized functions
     */
    @Overwrite
    public static void setupGuiFlatDiffuseLighting(Vector3f pLighting1, Vector3f pLighting2) {
        RenderSystem.assertOnRenderThread();
        Matrix4f matrix = new Matrix4f();
        matrix.setIdentity();
        MathHelper.getExtendedMatrix(matrix).scale(1.0F, -1.0F, 1.0F);
        matrix.multiply(CommonRotations.YN22_5);
        matrix.multiply(CommonRotations.XP135);
        setupLevelDiffuseLighting(pLighting1, pLighting2, matrix);
    }

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations
     */
    @Overwrite
    public static void setupLevelDiffuseLighting(Vector3f lightingVector1, Vector3f lightingVector2, Matrix4f matrix) {
        RenderSystem.assertOnRenderThread();
        IMatrix4fPatch matrixExt = MathHelper.getExtendedMatrix(matrix);
        float x0 = matrixExt.transformVecX(lightingVector1.x(), lightingVector1.y(), lightingVector1.z());
        float y0 = matrixExt.transformVecY(lightingVector1.x(), lightingVector1.y(), lightingVector1.z());
        float z0 = matrixExt.transformVecZ(lightingVector1.x(), lightingVector1.y(), lightingVector1.z());
        float x1 = matrixExt.transformVecX(lightingVector2.x(), lightingVector2.y(), lightingVector2.z());
        float y1 = matrixExt.transformVecY(lightingVector2.x(), lightingVector2.y(), lightingVector2.z());
        float z1 = matrixExt.transformVecZ(lightingVector2.x(), lightingVector2.y(), lightingVector2.z());
        RenderSystem.shaderLightDirections[0].set(x0, y0, z0);
        RenderSystem.shaderLightDirections[1].set(x1, y1, z1);
    }
}
