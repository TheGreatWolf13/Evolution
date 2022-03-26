package tgw.evolution.client.models.pipeline;

import com.mojang.blaze3d.vertex.VertexFormat;
import tgw.evolution.client.gl.IBufferVertexFormat;

public interface IVanillaVertexType<T extends IVertexSink> extends IBufferVertexType<T> {

    @Override
    default IBufferVertexFormat getBufferVertexFormat() {
        return IBufferVertexFormat.from(this.getVertexFormat());
    }

    VertexFormat getVertexFormat();
}
