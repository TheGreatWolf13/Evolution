package tgw.evolution.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Vec3i;
import net.minecraftforge.client.extensions.IForgeVertexConsumer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tgw.evolution.patches.IMatrix3fPatch;
import tgw.evolution.patches.IMatrix4fPatch;
import tgw.evolution.util.math.MathHelper;

@Mixin(IForgeVertexConsumer.class)
public interface IForgeVertexConsumerMixin {

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations.
     */
    @Overwrite
    default void putBulkData(PoseStack.Pose matrixEntry,
                             BakedQuad bakedQuad,
                             float red,
                             float green,
                             float blue,
                             float alpha,
                             int packedLight,
                             int packedOverlay,
                             boolean readExistingColor) {
        int[] vertices = bakedQuad.getVertices();
        Vec3i faceNormal = bakedQuad.getDirection().getNormal();
        IMatrix4fPatch poseMat = MathHelper.getExtendedMatrix(matrixEntry.pose());
        IMatrix3fPatch normalMat = MathHelper.getExtendedMatrix(matrixEntry.normal());
        float normX = normalMat.transformVecX(faceNormal.getX(), faceNormal.getY(), faceNormal.getZ());
        float normY = normalMat.transformVecY(faceNormal.getX(), faceNormal.getY(), faceNormal.getZ());
        float normZ = normalMat.transformVecZ(faceNormal.getX(), faceNormal.getY(), faceNormal.getZ());
        int vertexCount = vertices.length / 8;
        for (int vertex = 0; vertex < vertexCount; ++vertex) {
            int offset = vertex * 8;
            //Position : 3 float
            float posX = Float.intBitsToFloat(vertices[offset]);
            float posY = Float.intBitsToFloat(vertices[offset + 1]);
            float posZ = Float.intBitsToFloat(vertices[offset + 2]);
            float x = poseMat.transformVecX(posX, posY, posZ);
            float y = poseMat.transformVecY(posX, posY, posZ);
            float z = poseMat.transformVecZ(posX, posY, posZ);
            //Color : 4 byte
            float cr;
            float cg;
            float cb;
            float ca;
            if (readExistingColor) {
                int color = vertices[offset + 3];
                cr = (color >> 24 & 255) / 255.0F * red;
                cg = (color >> 16 & 255) / 255.0F * green;
                cb = (color >> 8 & 255) / 255.0F * blue;
                ca = (color & 255) / 255.0F * alpha;
            }
            else {
                cr = red;
                cg = green;
                cb = blue;
                ca = alpha;
            }
            //Texture : 2 float
            float u = Float.intBitsToFloat(vertices[offset + 4]);
            float v = Float.intBitsToFloat(vertices[offset + 5]);
            //Light : 2 short
            int bl = packedLight & 0xFFFF;
            int sl = packedLight >> 16 & 0xFFFF;
            int light = vertices[offset + 6];
            int blBaked = light & 0xffff;
            int slBaked = light >> 16 & 0xffff;
            bl = Math.max(bl, blBaked);
            sl = Math.max(sl, slBaked);
            int lightmapCoord = bl | sl << 16;
            //Normal : 3 byte
            int norm = vertices[offset + 7];
            byte nx = (byte) (norm & 255);
            byte ny = (byte) (norm >> 8 & 255);
            byte nz = (byte) (norm >> 16 & 255);
            if (nx != 0 || ny != 0 || nz != 0) {
                normX = nx / 127.0f;
                normY = ny / 127.0f;
                normZ = nz / 127.0f;
                float nX = normalMat.transformVecX(normX, normY, normZ);
                float nY = normalMat.transformVecY(normX, normY, normZ);
                float nZ = normalMat.transformVecZ(normX, normY, normZ);
                normX = nX;
                normY = nY;
                normZ = nZ;
            }
            ((VertexConsumer) this).vertex(x, y, z, cr, cg, cb, ca, u, v, packedOverlay, lightmapCoord, normX, normY, normZ);
        }
    }

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations and inline.
     */
    @Overwrite
    default void putBulkData(PoseStack.Pose matrixEntry,
                             BakedQuad bakedQuad,
                             float red,
                             float green,
                             float blue,
                             float alpha,
                             int packedLight,
                             int packedOverlay) {
        int[] vertices = bakedQuad.getVertices();
        Vec3i faceNormal = bakedQuad.getDirection().getNormal();
        IMatrix4fPatch poseMat = MathHelper.getExtendedMatrix(matrixEntry.pose());
        IMatrix3fPatch normalMat = MathHelper.getExtendedMatrix(matrixEntry.normal());
        float normX = normalMat.transformVecX(faceNormal.getX(), faceNormal.getY(), faceNormal.getZ());
        float normY = normalMat.transformVecY(faceNormal.getX(), faceNormal.getY(), faceNormal.getZ());
        float normZ = normalMat.transformVecZ(faceNormal.getX(), faceNormal.getY(), faceNormal.getZ());
        int vertexCount = vertices.length / 8;
        for (int vertex = 0; vertex < vertexCount; ++vertex) {
            int offset = vertex * 8;
            //Position : 3 float
            float posX = Float.intBitsToFloat(vertices[offset]);
            float posY = Float.intBitsToFloat(vertices[offset + 1]);
            float posZ = Float.intBitsToFloat(vertices[offset + 2]);
            float x = poseMat.transformVecX(posX, posY, posZ);
            float y = poseMat.transformVecY(posX, posY, posZ);
            float z = poseMat.transformVecZ(posX, posY, posZ);
            //Texture : 2 float
            float u = Float.intBitsToFloat(vertices[offset + 4]);
            float v = Float.intBitsToFloat(vertices[offset + 5]);
            //Light : 2 short
            int bl = packedLight & 0xFFFF;
            int sl = packedLight >> 16 & 0xFFFF;
            int light = vertices[offset + 6];
            int blBaked = light & 0xffff;
            int slBaked = light >> 16 & 0xffff;
            bl = Math.max(bl, blBaked);
            sl = Math.max(sl, slBaked);
            int lightmapCoord = bl | sl << 16;
            //Normal : 3 byte
            int norm = vertices[offset + 7];
            byte nx = (byte) (norm & 255);
            byte ny = (byte) (norm >> 8 & 255);
            byte nz = (byte) (norm >> 16 & 255);
            if (nx != 0 || ny != 0 || nz != 0) {
                normX = nx / 127.0f;
                normY = ny / 127.0f;
                normZ = nz / 127.0f;
                float nX = normalMat.transformVecX(normX, normY, normZ);
                float nY = normalMat.transformVecY(normX, normY, normZ);
                float nZ = normalMat.transformVecZ(normX, normY, normZ);
                normX = nX;
                normY = nY;
                normZ = nZ;
            }
            ((VertexConsumer) this).vertex(x, y, z, red, green, blue, alpha, u, v, packedOverlay, lightmapCoord, normX, normY, normZ);
        }
    }

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations.
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
        IMatrix4fPatch poseMat = MathHelper.getExtendedMatrix(matrixEntry.pose());
        IMatrix3fPatch normalMat = MathHelper.getExtendedMatrix(matrixEntry.normal());
        float normX = normalMat.transformVecX(faceNormal.getX(), faceNormal.getY(), faceNormal.getZ());
        float normY = normalMat.transformVecY(faceNormal.getX(), faceNormal.getY(), faceNormal.getZ());
        float normZ = normalMat.transformVecZ(faceNormal.getX(), faceNormal.getY(), faceNormal.getZ());
        int vertexCount = vertices.length / 8;
        for (int vertex = 0; vertex < vertexCount; ++vertex) {
            int offset = vertex * 8;
            //Position : 3 float
            float posX = Float.intBitsToFloat(vertices[offset]);
            float posY = Float.intBitsToFloat(vertices[offset + 1]);
            float posZ = Float.intBitsToFloat(vertices[offset + 2]);
            float x = poseMat.transformVecX(posX, posY, posZ);
            float y = poseMat.transformVecY(posX, posY, posZ);
            float z = poseMat.transformVecZ(posX, posY, posZ);
            //Color : 4 byte
            float cr;
            float cg;
            float cb;
            float ca;
            if (readExistingColor) {
                int color = vertices[offset + 3];
                cr = (color >> 24 & 255) / 255.0F * baseBrightness[vertex] * red;
                cg = (color >> 16 & 255) / 255.0F * baseBrightness[vertex] * green;
                cb = (color >> 8 & 255) / 255.0F * baseBrightness[vertex] * blue;
                ca = (color & 255) / 255.0F * alpha;
            }
            else {
                cr = baseBrightness[vertex] * red;
                cg = baseBrightness[vertex] * green;
                cb = baseBrightness[vertex] * blue;
                ca = alpha;
            }
            //Texture : 2 float
            float u = Float.intBitsToFloat(vertices[offset + 4]);
            float v = Float.intBitsToFloat(vertices[offset + 5]);
            //Light : 2 short
            int packedLight = lightmapCoords[vertex];
            int bl = packedLight & 0xFFFF;
            int sl = packedLight >> 16 & 0xFFFF;
            int light = vertices[offset + 6];
            int blBaked = light & 0xffff;
            int slBaked = light >> 16 & 0xffff;
            bl = Math.max(bl, blBaked);
            sl = Math.max(sl, slBaked);
            int lightmapCoord = bl | sl << 16;
            //Normal : 3 byte
            int norm = vertices[offset + 7];
            byte nx = (byte) (norm & 255);
            byte ny = (byte) (norm >> 8 & 255);
            byte nz = (byte) (norm >> 16 & 255);
            if (nx != 0 || ny != 0 || nz != 0) {
                normX = nx / 127.0f;
                normY = ny / 127.0f;
                normZ = nz / 127.0f;
                float nX = normalMat.transformVecX(normX, normY, normZ);
                float nY = normalMat.transformVecY(normX, normY, normZ);
                float nZ = normalMat.transformVecZ(normX, normY, normZ);
                normX = nX;
                normY = nY;
                normZ = nZ;
            }
            ((VertexConsumer) this).vertex(x, y, z, cr, cg, cb, ca, u, v, overlayCoords, lightmapCoord, normX, normY, normZ);
        }
    }
}
