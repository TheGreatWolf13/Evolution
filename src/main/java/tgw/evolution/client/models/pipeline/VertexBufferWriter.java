package tgw.evolution.client.models.pipeline;

import tgw.evolution.client.gl.IBufferVertexFormat;

public abstract class VertexBufferWriter implements IVertexSink {

    protected final IVertexBufferView backingBuffer;
    protected final IBufferVertexFormat vertexFormat;
    protected final int vertexStride;
    private int vertexCount;
    private int vertexCountFlushed;

    protected VertexBufferWriter(IVertexBufferView backingBuffer, IBufferVertexType<?> vertexType) {
        this.backingBuffer = backingBuffer;
        this.vertexFormat = vertexType.getBufferVertexFormat();
        this.vertexStride = this.vertexFormat.getStride();
        this.onBufferStorageChanged();
    }

    /**
     * Advances the write pointer forward by the stride of one vertex. This should always be called after a
     * vertex is written. Implementations which override this should always call invoke the super implementation.
     */
    protected void advance() {
        this.vertexCount++;
    }

    @Override
    public void ensureCapacity(int count) {
        if (this.backingBuffer.ensureBufferCapacity((this.vertexCount + count) * this.vertexStride)) {
            this.onBufferStorageChanged();
        }
    }

    @Override
    public void flush() {
        this.backingBuffer.flush(this.vertexCount, this.vertexFormat);
        this.vertexCountFlushed += this.vertexCount;
        this.vertexCount = 0;
    }

    @Override
    public int getVertexCount() {
        return this.vertexCountFlushed + this.vertexCount;
    }

    /**
     * Called when the underlying memory buffer to the backing storage changes. When this is called, the implementation
     * should update any pointers
     */
    protected abstract void onBufferStorageChanged();
}
