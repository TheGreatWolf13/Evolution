package tgw.evolution.client.models.pipeline;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;

public class QuadVertexType implements IVanillaVertexType<IQuadVertexSink>, IBlittableVertexType<IQuadVertexSink> {

    @Override
    public IBlittableVertexType<IQuadVertexSink> asBlittable() {
        return this;
    }

    @Override
    public IQuadVertexSink createBufferWriter(IVertexBufferView buffer, boolean direct) {
        return direct ? new QuadVertexBufferWriterUnsafe(buffer) : new QuadVertexBufferWriterNio(buffer);
    }

    @Override
    public IQuadVertexSink createFallbackWriter(VertexConsumer consumer) {
        return new QuadVertexWriterFallback(consumer);
    }

    @Override
    public VertexFormat getVertexFormat() {
        return IQuadVertexSink.VERTEX_FORMAT;
    }
}
