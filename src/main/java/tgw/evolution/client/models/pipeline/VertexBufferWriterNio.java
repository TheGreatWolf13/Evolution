package tgw.evolution.client.models.pipeline;

import java.nio.ByteBuffer;

public abstract class VertexBufferWriterNio extends VertexBufferWriter {
    protected ByteBuffer byteBuffer;
    protected int writeOffset;

    protected VertexBufferWriterNio(IVertexBufferView backingBuffer, IBufferVertexType<?> vertexType) {
        super(backingBuffer, vertexType);
    }

    @Override
    protected void advance() {
        this.writeOffset += this.vertexStride;
        super.advance();
    }

    @Override
    protected void onBufferStorageChanged() {
        this.byteBuffer = this.backingBuffer.getDirectBuffer();
        this.writeOffset = this.backingBuffer.getWriterPosition();
    }
}
