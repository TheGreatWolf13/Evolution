package tgw.evolution.client.models.pipeline;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;

public class ParticleVertexType implements IVanillaVertexType<IParticleVertexSink>, IBlittableVertexType<IParticleVertexSink> {

    @Override
    public IBlittableVertexType<IParticleVertexSink> asBlittable() {
        return this;
    }

    @Override
    public IParticleVertexSink createBufferWriter(IVertexBufferView buffer, boolean direct) {
        return direct ? new ParticleVertexBufferWriterUnsafe(buffer) : new ParticleVertexBufferWriterNio(buffer);
    }

    @Override
    public IParticleVertexSink createFallbackWriter(VertexConsumer consumer) {
        return new ParticleVertexWriterFallback(consumer);
    }

    @Override
    public VertexFormat getVertexFormat() {
        return IParticleVertexSink.VERTEX_FORMAT;
    }
}
