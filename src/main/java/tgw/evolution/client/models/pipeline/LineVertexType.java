package tgw.evolution.client.models.pipeline;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;

public class LineVertexType implements IVanillaVertexType<ILineVertexSink>, IBlittableVertexType<ILineVertexSink> {

    @Override
    public IBlittableVertexType<ILineVertexSink> asBlittable() {
        return this;
    }

    @Override
    public ILineVertexSink createBufferWriter(IVertexBufferView buffer, boolean direct) {
        return direct ? new LineVertexBufferWriterUnsafe(buffer) : new LineVertexBufferWriterNio(buffer);
    }

    @Override
    public ILineVertexSink createFallbackWriter(VertexConsumer consumer) {
        return new LineVertexWriterFallback(consumer);
    }

    @Override
    public VertexFormat getVertexFormat() {
        return ILineVertexSink.VERTEX_FORMAT;
    }
}
