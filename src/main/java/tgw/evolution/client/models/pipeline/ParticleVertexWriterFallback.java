package tgw.evolution.client.models.pipeline;

import com.mojang.blaze3d.vertex.VertexConsumer;
import tgw.evolution.util.math.ColorABGR;

public class ParticleVertexWriterFallback extends VertexWriterFallback implements IParticleVertexSink {

    public ParticleVertexWriterFallback(VertexConsumer consumer) {
        super(consumer);
    }

    @Override
    public void writeParticle(float x, float y, float z, float u, float v, int color, int light) {
        VertexConsumer consumer = this.consumer;
        consumer.vertex(x, y, z);
        consumer.uv(u, v);
        consumer.color(ColorABGR.unpackRed(color), ColorABGR.unpackGreen(color), ColorABGR.unpackBlue(color), ColorABGR.unpackAlpha(color));
        consumer.uv2(light);
        consumer.endVertex();
    }
}
