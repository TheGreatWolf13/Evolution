package tgw.evolution.client.models.pipeline;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import tgw.evolution.patches.IMatrix4fPatch;
import tgw.evolution.util.math.MathHelper;

public interface IQuadVertexSink extends IVertexSink {

    VertexFormat VERTEX_FORMAT = DefaultVertexFormat.NEW_ENTITY;

    /**
     * Writes a quad vertex to this sink.
     *
     * @param x       The x-position of the vertex
     * @param y       The y-position of the vertex
     * @param z       The z-position of the vertex
     * @param color   The ABGR-packed color of the vertex
     * @param u       The u-texture of the vertex
     * @param v       The y-texture of the vertex
     * @param light   The packed light-map coordinates of the vertex
     * @param overlay The packed overlay-map coordinates of the vertex
     * @param normal  The 3-byte packed normal vector of the vertex
     */
    void writeQuad(float x, float y, float z, int color, float u, float v, int light, int overlay, int normal);

    /**
     * Writes a quad vertex to the sink, transformed by the given matrices.
     *
     * @param matrices The matrices to transform the vertex's position and normal vectors by
     */
    default void writeQuad(PoseStack.Pose matrices, float x, float y, float z, int color, float u, float v, int light, int overlay, int normal) {
        IMatrix4fPatch positionMatrix = MathHelper.getExtendedMatrix(matrices.pose());
        float x2 = positionMatrix.transformVecX(x, y, z);
        float y2 = positionMatrix.transformVecY(x, y, z);
        float z2 = positionMatrix.transformVecZ(x, y, z);
        int norm = MathHelper.transformPackedNormal(normal, matrices.normal());
        this.writeQuad(x2, y2, z2, color, u, v, light, overlay, norm);
    }
}
