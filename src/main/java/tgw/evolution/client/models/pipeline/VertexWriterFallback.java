package tgw.evolution.client.models.pipeline;

import com.mojang.blaze3d.vertex.VertexConsumer;

public abstract class VertexWriterFallback implements IVertexSink {

    protected final VertexConsumer consumer;

    protected VertexWriterFallback(VertexConsumer consumer) {
        this.consumer = consumer;
    }

    @Override
    public void ensureCapacity(int count) {
        // NO-OP
    }

    @Override
    public void flush() {
        // NO-OP
    }

    @Override
    public int getVertexCount() {
        return 0;
    }
}
