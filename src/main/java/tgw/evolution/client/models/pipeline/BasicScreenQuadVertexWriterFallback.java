package tgw.evolution.client.models.pipeline;

import com.mojang.blaze3d.vertex.VertexConsumer;
import tgw.evolution.util.math.ColorABGR;

public class BasicScreenQuadVertexWriterFallback extends VertexWriterFallback implements IBasicScreenQuadVertexSink {

    public BasicScreenQuadVertexWriterFallback(VertexConsumer consumer) {
        super(consumer);
    }

    @Override
    public void writeQuad(float x, float y, float z, int color) {
        VertexConsumer consumer = this.consumer;
        consumer.vertex(x, y, z);
        consumer.color(ColorABGR.unpackRed(color), ColorABGR.unpackGreen(color), ColorABGR.unpackBlue(color), ColorABGR.unpackAlpha(color));
        consumer.endVertex();
    }
}
