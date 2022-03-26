package tgw.evolution.client.gl;

import com.mojang.blaze3d.vertex.VertexFormat;

public interface IBufferVertexFormat {

    static IBufferVertexFormat from(VertexFormat format) {
        return (IBufferVertexFormat) format;
    }

    int getStride();
}
