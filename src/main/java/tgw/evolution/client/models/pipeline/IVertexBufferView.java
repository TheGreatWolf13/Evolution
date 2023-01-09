package tgw.evolution.client.models.pipeline;

import tgw.evolution.client.gl.IBufferVertexFormat;

import java.nio.ByteBuffer;

public interface IVertexBufferView {

    /**
     * Ensures there is capacity in the buffer for the given number of bytes.
     *
     * @param bytes The number of bytes to allocate space for
     * @return True if the buffer was resized, otherwise false
     */
    boolean ensureBufferCapacity(int bytes);

    /**
     * Flushes the given number of vertices to this buffer. This ensures that all constraints are still valid, and if
     * so, advances the vertex counter and writer pointer to the end of the data that was written by the caller.
     *
     * @param vertexCount The number of vertices to flush
     * @param format      The format of each vertex
     */
    void flush(int vertexCount, IBufferVertexFormat format);

    /**
     * Returns a handle to the internal storage of this buffer. The buffer can be directly written into at the
     * base address provided by {@link IVertexBufferView#getWriterPosition()}.
     *
     * @return A {@link ByteBuffer} in off-heap space
     */
    ByteBuffer getDirectBuffer();

    /**
     * @return The position at which new data should be written to, in bytes
     */
    int getWriterPosition();

    /**
     * @return The current vertex format of the buffer
     */
    IBufferVertexFormat vertexFormat();
}
