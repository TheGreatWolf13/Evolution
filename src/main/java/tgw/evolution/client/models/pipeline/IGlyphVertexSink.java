package tgw.evolution.client.models.pipeline;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import tgw.evolution.patches.IMatrix4fPatch;
import tgw.evolution.util.math.MathHelper;

public interface IGlyphVertexSink extends IVertexSink {

    VertexFormat VERTEX_FORMAT = DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP;

    /**
     * Writes a glyph vertex to the sink.
     *
     * @param matrix The transformation matrix to apply to the vertex's position
     * @see IGlyphVertexSink#writeGlyph(float, float, float, int, float, float, int)
     */
    default void writeGlyph(Matrix4f matrix, float x, float y, float z, int color, float u, float v, int light) {
        IMatrix4fPatch matrixExt = MathHelper.getExtendedMatrix(matrix);
        float x2 = matrixExt.transformVecX(x, y, z);
        float y2 = matrixExt.transformVecY(x, y, z);
        float z2 = matrixExt.transformVecZ(x, y, z);
        this.writeGlyph(x2, y2, z2, color, u, v, light);
    }

    /**
     * Writes a glyph vertex to the sink.
     *
     * @param x     The x-position of the vertex
     * @param y     The y-position of the vertex
     * @param z     The z-position of the vertex
     * @param color The ABGR-packed color of the vertex
     * @param u     The u-texture of the vertex
     * @param v     The v-texture of the vertex
     * @param light The packed light map texture coordinates of the vertex
     */
    void writeGlyph(float x, float y, float z, int color, float u, float v, int light);
}
