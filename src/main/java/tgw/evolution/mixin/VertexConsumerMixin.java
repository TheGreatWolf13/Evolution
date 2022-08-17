package tgw.evolution.mixin;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Vec3i;
import net.minecraftforge.client.extensions.IForgeVertexConsumer;
import org.lwjgl.system.MemoryStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.client.models.pipeline.IVertexDrain;
import tgw.evolution.client.models.pipeline.IVertexSink;
import tgw.evolution.client.models.pipeline.IVertexType;
import tgw.evolution.client.renderer.RenderHelper;
import tgw.evolution.patches.IMatrix3fPatch;
import tgw.evolution.patches.IMatrix4fPatch;
import tgw.evolution.util.math.MathHelper;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;

@Mixin(VertexConsumer.class)
public interface VertexConsumerMixin extends IForgeVertexConsumer, IVertexDrain {

    @Override
    default <T extends IVertexSink> T createSink(IVertexType<T> factory) {
        return factory.createFallbackWriter((VertexConsumer) this);
    }

    /**
     * @reason Avoid allocations
     * @author JellySquid
     */
    @Overwrite
    default VertexConsumer normal(Matrix3f matrix, float x, float y, float z) {
        IMatrix3fPatch ext = MathHelper.getExtendedMatrix(matrix);
        float x2 = ext.transformVecX(x, y, z);
        float y2 = ext.transformVecY(x, y, z);
        float z2 = ext.transformVecZ(x, y, z);
        return this.normal(x2, y2, z2);
    }

    @Shadow
    VertexConsumer normal(float x, float y, float z);

    /**
     * @author TheGreatWolf
     * <p>
     * Avoid allocations.
     */
    @Overwrite
    default void putBulkData(PoseStack.Pose matrix, BakedQuad quad, float red, float green, float blue, int combinedLight, int combinedOverlay) {
        int[] lightmap = RenderHelper.LIGHTMAP.get();
        Arrays.fill(lightmap, combinedLight);
        this.putBulkData(matrix, quad, RenderHelper.DEF_BRIGHTNESS, red, green, blue, lightmap, combinedOverlay, false);
    }

    /**
     * @author TheGreatWolf
     * <p>
     * Avoid allocations
     */
    @Overwrite
    default void putBulkData(PoseStack.Pose entry,
                             BakedQuad quad,
                             float[] baseBrightness,
                             float red,
                             float green,
                             float blue,
                             int[] light,
                             int overlay,
                             boolean useBaseColors) {
        int[] aint = quad.getVertices();
        Vec3i vector3i = quad.getDirection().getNormal();
        Vector3f normal = new Vector3f(vector3i.getX(), vector3i.getY(), vector3i.getZ());
        Matrix4f pose = entry.pose();
        IMatrix4fPatch poseExt = MathHelper.getExtendedMatrix(pose);
        normal.transform(entry.normal());
        int j = aint.length / 8;
        try (MemoryStack memorystack = MemoryStack.stackPush()) {
            ByteBuffer bytebuffer = memorystack.malloc(DefaultVertexFormat.BLOCK.getVertexSize());
            IntBuffer intbuffer = bytebuffer.asIntBuffer();
            for (int k = 0; k < j; ++k) {
                intbuffer.clear();
                intbuffer.put(aint, k * 8, 8);
                float x = bytebuffer.getFloat(0);
                float y = bytebuffer.getFloat(4);
                float z = bytebuffer.getFloat(8);
                float f3;
                float f4;
                float f5;
                if (useBaseColors) {
                    float f6 = (bytebuffer.get(12) & 255) / 255.0F;
                    float f7 = (bytebuffer.get(13) & 255) / 255.0F;
                    float f8 = (bytebuffer.get(14) & 255) / 255.0F;
                    f3 = f6 * baseBrightness[k] * red;
                    f4 = f7 * baseBrightness[k] * green;
                    f5 = f8 * baseBrightness[k] * blue;
                }
                else {
                    f3 = baseBrightness[k] * red;
                    f4 = baseBrightness[k] * green;
                    f5 = baseBrightness[k] * blue;
                }
                int l = this.applyBakedLighting(light[k], bytebuffer);
                float f9 = bytebuffer.getFloat(16);
                float f10 = bytebuffer.getFloat(20);
                float x2 = poseExt.transformVecX(x, y, z);
                float y2 = poseExt.transformVecY(x, y, z);
                float z2 = poseExt.transformVecZ(x, y, z);
                this.applyBakedNormals(normal, bytebuffer, entry.normal());
                this.vertex(x2, y2, z2, f3, f4, f5, 1.0F, f9, f10, overlay, l, normal.x(), normal.y(), normal.z());
            }
        }
    }

    @Shadow
    void vertex(float p_225588_1_,
                float p_225588_2_,
                float p_225588_3_,
                float p_225588_4_,
                float p_225588_5_,
                float p_225588_6_,
                float p_225588_7_,
                float p_225588_8_,
                float p_225588_9_,
                int p_225588_10_,
                int p_225588_11_,
                float p_225588_12_,
                float p_225588_13_,
                float p_225588_14_);

    /**
     * @author JellySquid
     * <p>
     * Avoid allocations
     */
    @Overwrite
    default VertexConsumer vertex(Matrix4f matrix, float x, float y, float z) {
        IMatrix4fPatch ext = MathHelper.getExtendedMatrix(matrix);
        float x2 = ext.transformVecX(x, y, z);
        float y2 = ext.transformVecY(x, y, z);
        float z2 = ext.transformVecZ(x, y, z);
        return this.vertex(x2, y2, z2);
    }

    @Shadow
    VertexConsumer vertex(double x, double y, double z);
}
