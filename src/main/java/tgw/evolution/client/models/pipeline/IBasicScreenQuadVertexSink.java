package tgw.evolution.client.models.pipeline;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import tgw.evolution.patches.IMatrix4fPatch;
import tgw.evolution.util.math.MathHelper;

public interface IBasicScreenQuadVertexSink extends IVertexSink {

    VertexFormat VERTEX_FORMAT = DefaultVertexFormat.POSITION_COLOR;

    /**
     * Writes a quad vertex to this sink.
     *
     * @param x     The x-position of the vertex
     * @param y     The y-position of the vertex
     * @param z     The z-position of the vertex
     * @param color The ABGR-packed color of the vertex
     */
    void writeQuad(float x, float y, float z, int color);

    /**
     * Writes a quad vertex to the sink, transformed by the given matrix.
     *
     * @param matrix The matrix to transform the vertex's position by
     */
    default void writeQuad(Matrix4f matrix, float x, float y, float z, int color) {
        IMatrix4fPatch modelMatrix = MathHelper.getExtendedMatrix(matrix);
        float x2 = modelMatrix.transformVecX(x, y, z);
        float y2 = modelMatrix.transformVecY(x, y, z);
        float z2 = modelMatrix.transformVecZ(x, y, z);
        this.writeQuad(x2, y2, z2, color);
    }
}
