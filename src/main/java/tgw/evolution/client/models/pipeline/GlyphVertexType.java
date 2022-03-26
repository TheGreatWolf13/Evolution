package tgw.evolution.client.models.pipeline;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;

public class GlyphVertexType implements IVanillaVertexType<IGlyphVertexSink>, IBlittableVertexType<IGlyphVertexSink> {

    @Override
    public IBlittableVertexType<IGlyphVertexSink> asBlittable() {
        return this;
    }

    @Override
    public IGlyphVertexSink createBufferWriter(IVertexBufferView buffer, boolean direct) {
        return direct ? new GlyphVertexBufferWriterUnsafe(buffer) : new GlyphVertexBufferWriterNio(buffer);
    }

    @Override
    public IGlyphVertexSink createFallbackWriter(VertexConsumer consumer) {
        return new GlyphVertexWriterFallback(consumer);
    }

    @Override
    public VertexFormat getVertexFormat() {
        return IGlyphVertexSink.VERTEX_FORMAT;
    }
}
