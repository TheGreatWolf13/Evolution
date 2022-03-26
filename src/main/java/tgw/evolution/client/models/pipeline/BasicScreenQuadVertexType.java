package tgw.evolution.client.models.pipeline;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;

public class BasicScreenQuadVertexType implements IVanillaVertexType<IBasicScreenQuadVertexSink>, IBlittableVertexType<IBasicScreenQuadVertexSink> {

    @Override
    public IBlittableVertexType<IBasicScreenQuadVertexSink> asBlittable() {
        return this;
    }

    @Override
    public IBasicScreenQuadVertexSink createBufferWriter(IVertexBufferView buffer, boolean direct) {
        return direct ? new BasicScreenQuadVertexBufferWriterUnsafe(buffer) : new BasicScreenQuadVertexBufferWriterNio(buffer);
    }

    @Override
    public IBasicScreenQuadVertexSink createFallbackWriter(VertexConsumer consumer) {
        return new BasicScreenQuadVertexWriterFallback(consumer);
    }

    @Override
    public VertexFormat getVertexFormat() {
        return IBasicScreenQuadVertexSink.VERTEX_FORMAT;
    }
}
