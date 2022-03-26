package tgw.evolution.client.models.pipeline;

import org.lwjgl.system.MemoryUtil;

public abstract class VertexBufferWriterUnsafe extends VertexBufferWriter {

    /**
     * The write pointer into the buffer storage. This is advanced by the vertex stride every time
     * {@link VertexBufferWriterUnsafe#advance()} is called.
     */
    protected long writePointer;

    protected VertexBufferWriterUnsafe(IVertexBufferView backingBuffer, IBufferVertexType<?> vertexType) {
        super(backingBuffer, vertexType);
    }

    @Override
    protected void advance() {
        this.writePointer += this.vertexStride;
        super.advance();
    }

    @Override
    protected void onBufferStorageChanged() {
        this.writePointer = MemoryUtil.memAddress(this.backingBuffer.getDirectBuffer(), this.backingBuffer.getWriterPosition());
    }
}
