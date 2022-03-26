package tgw.evolution.client.models.pipeline;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;

public interface IParticleVertexSink extends IVertexSink {

    VertexFormat VERTEX_FORMAT = DefaultVertexFormat.PARTICLE;

    /**
     * @param x     The x-position of the vertex
     * @param y     The y-position of the vertex
     * @param z     The z-position of the vertex
     * @param u     The u-texture of the vertex
     * @param v     The v-texture of the vertex
     * @param color The ABGR-packed color of the vertex
     * @param light The packed light map texture coordinates of the vertex
     */
    void writeParticle(float x, float y, float z, float u, float v, int color, int light);
}
