package tgw.evolution.mixin;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraftforge.client.extensions.IForgeVertexBuilder;
import org.lwjgl.system.MemoryStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.patches.IMatrix3fPatch;
import tgw.evolution.patches.IMatrix4fPatch;
import tgw.evolution.util.MathHelper;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

@Mixin(IVertexBuilder.class)
public interface IVertexBuilderMixin extends IForgeVertexBuilder {

    @Shadow
    IVertexBuilder normal(float p_225584_1_, float p_225584_2_, float p_225584_3_);

    /**
     * @reason Avoid allocations
     * @author JellySquid
     */
    @Overwrite
    default IVertexBuilder normal(Matrix3f matrix, float x, float y, float z) {
        IMatrix3fPatch ext = MathHelper.getExtendedMatrix(matrix);
        float x2 = ext.transformVecX(x, y, z);
        float y2 = ext.transformVecY(x, y, z);
        float z2 = ext.transformVecZ(x, y, z);
        return this.normal(x2, y2, z2);
    }

    /**
     * @reason Avoid allocations
     * @author MGSchultz
     */
    @Overwrite
    default void putBulkData(MatrixStack.Entry matrices,
                             BakedQuad quad,
                             float[] p_227890_3_,
                             float p_227890_4_,
                             float p_227890_5_,
                             float p_227890_6_,
                             int[] p_227890_7_,
                             int p_227890_8_,
                             boolean p_227890_9_) {
        int[] aint = quad.getVertices();
        Vector3i vector3i = quad.getDirection().getNormal();
        Vector3f vector3f = new Vector3f(vector3i.getX(), vector3i.getY(), vector3i.getZ());
        Matrix4f matrix4f = matrices.pose();
        vector3f.transform(matrices.normal());
        int j = aint.length / 8;
        try (MemoryStack memorystack = MemoryStack.stackPush()) {
            ByteBuffer bytebuffer = memorystack.malloc(DefaultVertexFormats.BLOCK.getVertexSize());
            IntBuffer intbuffer = bytebuffer.asIntBuffer();
            for (int k = 0; k < j; ++k) {
                intbuffer.clear();
                intbuffer.put(aint, k * 8, 8);
                float f = bytebuffer.getFloat(0);
                float f1 = bytebuffer.getFloat(4);
                float f2 = bytebuffer.getFloat(8);
                float f3;
                float f4;
                float f5;
                if (p_227890_9_) {
                    float f6 = (bytebuffer.get(12) & 255) / 255.0F;
                    float f7 = (bytebuffer.get(13) & 255) / 255.0F;
                    float f8 = (bytebuffer.get(14) & 255) / 255.0F;
                    f3 = f6 * p_227890_3_[k] * p_227890_4_;
                    f4 = f7 * p_227890_3_[k] * p_227890_5_;
                    f5 = f8 * p_227890_3_[k] * p_227890_6_;
                }
                else {
                    f3 = p_227890_3_[k] * p_227890_4_;
                    f4 = p_227890_3_[k] * p_227890_5_;
                    f5 = p_227890_3_[k] * p_227890_6_;
                }
                int l = this.applyBakedLighting(p_227890_7_[k], bytebuffer);
                float f9 = bytebuffer.getFloat(16);
                float f10 = bytebuffer.getFloat(20);
                IMatrix4fPatch mat4 = MathHelper.getExtendedMatrix(matrix4f);
                float x2 = mat4.transformVecX(f, f1, f2);
                float y2 = mat4.transformVecY(f, f1, f2);
                float z2 = mat4.transformVecZ(f, f1, f2);
                this.applyBakedNormals(vector3f, bytebuffer, matrices.normal());
                this.vertex(x2, y2, z2, f3, f4, f5, 1.0F, f9, f10, p_227890_8_, l, vector3f.x(), vector3f.y(), vector3f.z());
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
     * @reason Avoid allocations
     * @author JellySquid
     */
    @Overwrite
    default IVertexBuilder vertex(Matrix4f matrix, float x, float y, float z) {
        IMatrix4fPatch ext = MathHelper.getExtendedMatrix(matrix);
        float x2 = ext.transformVecX(x, y, z);
        float y2 = ext.transformVecY(x, y, z);
        float z2 = ext.transformVecZ(x, y, z);
        return this.vertex(x2, y2, z2);
    }

    @Shadow
    IVertexBuilder vertex(double p_225582_1_, double p_225582_3_, double p_225582_5_);
}
