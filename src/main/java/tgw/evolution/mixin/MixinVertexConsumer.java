package tgw.evolution.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Vec3i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.patches.PatchVertexConsumer;

@Mixin(VertexConsumer.class)
public interface MixinVertexConsumer extends PatchVertexConsumer {

    /**
     * @reason Avoid allocations
     * @author JellySquid
     */
    @Overwrite
    default VertexConsumer normal(Matrix3f matrix, float x, float y, float z) {
        float x2 = matrix.transformVecX(x, y, z);
        float y2 = matrix.transformVecY(x, y, z);
        float z2 = matrix.transformVecZ(x, y, z);
        return this.normal(x2, y2, z2);
    }

    @Shadow
    VertexConsumer normal(float x, float y, float z);

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations and inline
     */
    @Overwrite
    default void putBulkData(PoseStack.Pose entry, BakedQuad quad, float red, float green, float blue, int combinedLight, int combinedOverlay) {
        int[] vertices = quad.getVertices();
        Vec3i faceNormal = quad.getDirection().getNormal();
        Matrix4f poseMat = entry.pose();
        Matrix3f normalMat = entry.normal();
        float normX = normalMat.transformVecX(faceNormal.getX(), faceNormal.getY(), faceNormal.getZ());
        float normY = normalMat.transformVecY(faceNormal.getX(), faceNormal.getY(), faceNormal.getZ());
        float normZ = normalMat.transformVecZ(faceNormal.getX(), faceNormal.getY(), faceNormal.getZ());
        int bl = combinedLight & 0xFFFF;
        int sl = combinedLight >> 16 & 0xFFFF;
        for (int vertex = 0; vertex < 4; ++vertex) {
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
            this.vertex(x, y, z, red, green, blue, 1.0F, u, v, combinedOverlay, lightmapCoord, normX, normY, normZ);
        }
    }

    @Override
    default void putBulkData(PoseStack.Pose entry,
                             BakedQuad quad,
                             float r,
                             float g,
                             float b,
                             float a,
                             int packedLight,
                             int packedOverlay,
                             boolean readExistingColor) {
        int[] vertices = quad.getVertices();
        Vec3i faceNormal = quad.getDirection().getNormal();
        Matrix4f poseMat = entry.pose();
        Matrix3f normalMat = entry.normal();
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
                cr = (color >> 24 & 255) / 255.0F * r;
                cg = (color >> 16 & 255) / 255.0F * g;
                cb = (color >> 8 & 255) / 255.0F * b;
                ca = (color & 255) / 255.0F * a;
            }
            else {
                cr = r;
                cg = g;
                cb = b;
                ca = a;
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
     * @reason Avoid allocations.
     */
    @Overwrite
    default void putBulkData(PoseStack.Pose entry,
                             BakedQuad quad,
                             float[] baseBrightness,
                             float red,
                             float green,
                             float blue,
                             int[] lightmapCoords,
                             int overlay,
                             boolean useBaseColors) {
        int[] vertices = quad.getVertices();
        Vec3i faceNormal = quad.getDirection().getNormal();
        Matrix4f poseMat = entry.pose();
        Matrix3f normalMat = entry.normal();
        float normX = normalMat.transformVecX(faceNormal.getX(), faceNormal.getY(), faceNormal.getZ());
        float normY = normalMat.transformVecY(faceNormal.getX(), faceNormal.getY(), faceNormal.getZ());
        float normZ = normalMat.transformVecZ(faceNormal.getX(), faceNormal.getY(), faceNormal.getZ());
        for (int vertex = 0; vertex < 4; ++vertex) {
            int offset = vertex * 8;
            //Position : 3 float
            float posX = Float.intBitsToFloat(vertices[offset]);
            float posY = Float.intBitsToFloat(vertices[offset + 1]);
            float posZ = Float.intBitsToFloat(vertices[offset + 2]);
            float x = poseMat.transformVecX(posX, posY, posZ);
            float y = poseMat.transformVecY(posX, posY, posZ);
            float z = poseMat.transformVecZ(posX, posY, posZ);
            //Color : 4 byte
            float bright = baseBrightness[vertex];
            float cr;
            float cg;
            float cb;
            if (useBaseColors) {
                int color = vertices[offset + 3];
                cr = (color >> 24 & 255) * (1 / 255.0F) * bright * red;
                cg = (color >> 16 & 255) * (1 / 255.0F) * bright * green;
                cb = (color >> 8 & 255) * (1 / 255.0F) * bright * blue;
            }
            else {
                cr = bright * red;
                cg = bright * green;
                cb = bright * blue;
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
            this.vertex(x, y, z, cr, cg, cb, 1.0F, u, v, overlay, lightmapCoord, normX, normY, normZ);
        }
    }

    @Override
    default void putBulkData(PoseStack.Pose entry,
                             BakedQuad quad,
                             float bright0,
                             float bright1,
                             float bright2,
                             float bright3,
                             float r,
                             float g,
                             float b,
                             int light0,
                             int light1,
                             int light2,
                             int light3,
                             int overlay) {
        int[] vertices = quad.getVertices();
        Vec3i faceNormal = quad.getDirection().getNormal();
        Matrix4f poseMat = entry.pose();
        Matrix3f normalMat = entry.normal();
        float normX = normalMat.transformVecX(faceNormal.getX(), faceNormal.getY(), faceNormal.getZ());
        float normY = normalMat.transformVecY(faceNormal.getX(), faceNormal.getY(), faceNormal.getZ());
        float normZ = normalMat.transformVecZ(faceNormal.getX(), faceNormal.getY(), faceNormal.getZ());
        for (int vertex = 0; vertex < 4; ++vertex) {
            float bright;
            int light;
            switch (vertex) {
                case 0 -> {
                    bright = bright0;
                    light = light0;
                }
                case 1 -> {
                    bright = bright1;
                    light = light1;
                }
                case 2 -> {
                    bright = bright2;
                    light = light2;
                }
                case 3 -> {
                    bright = bright3;
                    light = light3;
                }
                default -> throw new IncompatibleClassChangeError();
            }
            int offset = vertex * 8;
            //Position : 3 float
            float posX = Float.intBitsToFloat(vertices[offset]);
            float posY = Float.intBitsToFloat(vertices[offset + 1]);
            float posZ = Float.intBitsToFloat(vertices[offset + 2]);
            float x = poseMat.transformVecX(posX, posY, posZ);
            float y = poseMat.transformVecY(posX, posY, posZ);
            float z = poseMat.transformVecZ(posX, posY, posZ);
            //Color : 4 byte
            int color = vertices[offset + 3];
            float cr = (color >> 24 & 255) * (1 / 255.0F) * bright * r;
            float cg = (color >> 16 & 255) * (1 / 255.0F) * bright * g;
            float cb = (color >> 8 & 255) * (1 / 255.0F) * bright * b;
            //Texture : 2 float
            float u = Float.intBitsToFloat(vertices[offset + 4]);
            float v = Float.intBitsToFloat(vertices[offset + 5]);
            //Light : 2 short
            int packedLight = light;
            int bl = packedLight & 0xFFFF;
            int sl = packedLight >> 16 & 0xFFFF;
            int l = vertices[offset + 6];
            int blBaked = l & 0xffff;
            int slBaked = l >> 16 & 0xffff;
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
            this.vertex(x, y, z, cr, cg, cb, 1.0F, u, v, overlay, lightmapCoord, normX, normY, normZ);
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
     * @reason Avoid allocations
     */
    @Overwrite
    default VertexConsumer vertex(Matrix4f matrix, float x, float y, float z) {
        float x2 = matrix.transformVecX(x, y, z);
        float y2 = matrix.transformVecY(x, y, z);
        float z2 = matrix.transformVecZ(x, y, z);
        return this.vertex(x2, y2, z2);
    }

    @Shadow
    VertexConsumer vertex(double x, double y, double z);
}
