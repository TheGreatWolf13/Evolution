package tgw.evolution.client.models.pipeline;

import com.mojang.blaze3d.vertex.VertexConsumer;
import tgw.evolution.util.math.ColorABGR;
import tgw.evolution.util.math.Norm3b;

public class LineVertexWriterFallback extends VertexWriterFallback implements ILineVertexSink {

    public LineVertexWriterFallback(VertexConsumer consumer) {
        super(consumer);
    }

    @Override
    public void vertexLine(float x, float y, float z, int color, int normal) {
        VertexConsumer consumer = this.consumer;
        consumer.vertex(x, y, z);
        consumer.color(ColorABGR.unpackRed(color), ColorABGR.unpackGreen(color), ColorABGR.unpackBlue(color), ColorABGR.unpackAlpha(color));
        consumer.normal(Norm3b.unpackX(normal), Norm3b.unpackY(normal), Norm3b.unpackZ(normal));
        consumer.endVertex();
    }
}
