package tgw.evolution.client.models.pipeline;

import tgw.evolution.client.gl.IBufferVertexFormat;

public interface IBufferVertexType<T extends IVertexSink> extends IVertexType<T> {

    IBufferVertexFormat getBufferVertexFormat();
}
