package tgw.evolution.client.models.pipeline;

import com.mojang.blaze3d.vertex.VertexConsumer;
import tgw.evolution.util.math.ColorABGR;

public class GlyphVertexWriterFallback extends VertexWriterFallback implements IGlyphVertexSink {

    public GlyphVertexWriterFallback(VertexConsumer consumer) {
        super(consumer);
    }

    @Override
    public void writeGlyph(float x, float y, float z, int color, float u, float v, int light) {
        VertexConsumer consumer = this.consumer;
        consumer.vertex(x, y, z);
        consumer.color(ColorABGR.unpackRed(color), ColorABGR.unpackGreen(color), ColorABGR.unpackBlue(color), ColorABGR.unpackAlpha(color));
        consumer.uv(u, v);
        consumer.uv2(light);
        consumer.endVertex();
    }
}
