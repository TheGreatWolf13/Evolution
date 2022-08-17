package tgw.evolution.mixin;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Vec3i;
import net.minecraftforge.client.extensions.IForgeVertexConsumer;
import org.lwjgl.system.MemoryStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.client.renderer.RenderHelper;
import tgw.evolution.patches.IMatrix4fPatch;
import tgw.evolution.util.math.MathHelper;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;

@Mixin(IForgeVertexConsumer.class)
public interface IForgeVertexConsumerMixin {

    @Shadow
    int applyBakedLighting(int lightmapCoord, ByteBuffer data);

    @Shadow
    void applyBakedNormals(Vector3f generated, ByteBuffer data, Matrix3f normalTransform);

    /**
     * @author TheGreatWolf
     * <p>
     * Avoid allocations.
     */
    @Overwrite
    default void putBulkData(PoseStack.Pose matrixEntry,
                             BakedQuad bakedQuad,
                             float red,
                             float green,
                             float blue,
                             float alpha,
                             int lightmapCoord,
                             int overlayColor,
                             boolean readExistingColor) {
        int[] lightmap = RenderHelper.LIGHTMAP.get();
        Arrays.fill(lightmap, lightmapCoord);
        this.putBulkData(matrixEntry, bakedQuad, RenderHelper.DEF_BRIGHTNESS, red, green, blue, alpha, lightmap, overlayColor, readExistingColor);
    }

    /**
     * @author TheGreatWolf
     * <p>
     * Avoid allocations.
     */
    @Overwrite
    default void putBulkData(PoseStack.Pose matrixEntry,
                             BakedQuad bakedQuad,
                             float red,
                             float green,
                             float blue,
                             float alpha,
                             int lightmapCoord,
                             int overlayColor) {
        int[] lightmap = RenderHelper.LIGHTMAP.get();
        Arrays.fill(lightmap, lightmapCoord);
        this.putBulkData(matrixEntry, bakedQuad, RenderHelper.DEF_BRIGHTNESS, red, green, blue, alpha, lightmap, overlayColor, false);
    }

    /**
     * @author TheGreatWolf
     * <p>
     * Avoid allocations.
     */
    @Overwrite
    default void putBulkData(PoseStack.Pose matrixEntry,
                             BakedQuad bakedQuad,
                             float[] baseBrightness,
                             float red,
                             float green,
                             float blue,
                             float alpha,
                             int[] lightmapCoords,
                             int overlayCoords,
                             boolean readExistingColor) {
        int[] vertices = bakedQuad.getVertices();
        Vec3i faceNormal = bakedQuad.getDirection().getNormal();
        Vector3f normal = new Vector3f(faceNormal.getX(), faceNormal.getY(), faceNormal.getZ());
        IMatrix4fPatch pose = MathHelper.getExtendedMatrix(matrixEntry.pose());
        normal.transform(matrixEntry.normal());
        int intSize = DefaultVertexFormat.BLOCK.getIntegerSize();
        int vertexCount = vertices.length / intSize;
        try (MemoryStack memory = MemoryStack.stackPush()) {
            ByteBuffer buffer = memory.malloc(DefaultVertexFormat.BLOCK.getVertexSize());
            IntBuffer intBuffer = buffer.asIntBuffer();
            for (int vertex = 0; vertex < vertexCount; ++vertex) {
                intBuffer.clear();
                intBuffer.put(vertices, vertex * 8, 8);
                float f = buffer.getFloat(0);
                float f1 = buffer.getFloat(4);
                float f2 = buffer.getFloat(8);
                float cr;
                float cg;
                float cb;
                float ca;
                if (readExistingColor) {
                    float r = (buffer.get(12) & 255) / 255.0F;
                    float g = (buffer.get(13) & 255) / 255.0F;
                    float b = (buffer.get(14) & 255) / 255.0F;
                    float a = (buffer.get(15) & 255) / 255.0F;
                    cr = r * baseBrightness[vertex] * red;
                    cg = g * baseBrightness[vertex] * green;
                    cb = b * baseBrightness[vertex] * blue;
                    ca = a * alpha;
                }
                else {
                    cr = baseBrightness[vertex] * red;
                    cg = baseBrightness[vertex] * green;
                    cb = baseBrightness[vertex] * blue;
                    ca = alpha;
                }
                int lightmapCoord = this.applyBakedLighting(lightmapCoords[vertex], buffer);
                float u = buffer.getFloat(16);
                float v = buffer.getFloat(20);
                float x = pose.transformVecX(f, f1, f2);
                float y = pose.transformVecY(f, f1, f2);
                float z = pose.transformVecZ(f, f1, f2);
                this.applyBakedNormals(normal, buffer, matrixEntry.normal());
                ((VertexConsumer) this).vertex(x, y, z, cr, cg, cb, ca, u, v, overlayCoords, lightmapCoord, normal.x(), normal.y(), normal.z());
            }
        }
    }
}
